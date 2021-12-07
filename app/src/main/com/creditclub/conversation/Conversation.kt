package com.creditclub.conversation

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.cluster.R
import com.creditclub.core.data.api.CaseLogService
import com.creditclub.core.data.model.Feedback
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.data.request.CaseMessageThreadRequest
import com.creditclub.core.data.response.CaseResponse
import com.creditclub.core.model.CreditClubImage
import com.creditclub.core.util.format
import com.creditclub.core.util.getMessage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showInternalError
import com.creditclub.ui.*
import com.creditclub.ui.theme.elevatedSurface
import com.creditclub.ui.util.registerImagePicker
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

/**
 * Entry point for a conversation screen.
 *
 * @param navigateToProfile User action when navigation to a profile is requested
 * @param modifier [Modifier] to apply to this layout node
 * @param onNavIconPressed Sends an event up when the user clicks on the menu
 */
@Composable
fun ConversationContent(
    title: String,
    reference: String,
    fcmToken: String,
    navigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavIconPressed: () -> Unit = { },
    navController: NavController,
) {
    val emptyCase = remember { CaseResponse<List<Feedback>>(status = true, response = emptyList()) }
    val context = LocalContext.current
    val localStorage: LocalStorage by rememberBean()
    val scrollState = rememberLazyListState()
    val caseLogService: CaseLogService by rememberRetrofitService()
    val coroutineScope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val dialogProvider by rememberDialogProvider()
    val messageState: MutableState<List<Feedback>> = remember { mutableStateOf(emptyList()) }

    val imagePickerLauncher = registerImagePicker {
        val image = it.firstOrNull()
            ?: return@registerImagePicker dialogProvider.showInternalError()
        val creditClubImage = CreditClubImage(context, image)
        val message = Feedback(
            name = localStorage.agent!!.agentName!!,
            isAttachment = true,
            blobs = listOf(creditClubImage),
            dateLogged = Instant.now(),
            caseReference = reference,
        )
//        messages.value = listOf(message) + messages.value

        val (response, error) = safeRunIO {
            caseLogService.saveFeedback(message)
        }

        if (error != null) {
            dialogProvider.showError(error)
            return@registerImagePicker
        }

        if (response?.response != null) {
            messageState.value = listOf(response.response!!) + messageState.value
        }
    }

    val caseResponse by produceState(initialValue = emptyCase) {
        val request = CaseMessageThreadRequest(caseReference = reference)
        errorMessage = ""
        loading = true
        val (response, error) = safeRunIO {
            caseLogService.getCaseMessageThread(request)
        }
        loading = false

        if (error != null) {
            errorMessage = error.getMessage(context)
            return@produceState
        }
        if (response == null) {
            errorMessage = "No response from server"
            return@produceState
        }

        value = response
        messageState.value = response.response!!.reversed()
    }
    val isResolved = caseResponse.isResolved
    val isClosed = caseResponse.isClosed

    val sendMessage: suspend (newMessage: Feedback) -> Unit = remember {
        sendMessage@{ newMessage ->
            val (_, error) = safeRunIO {
                caseLogService.saveFeedback(newMessage)
            }

            if (error != null) {
                errorMessage = error.getMessage(context)
                return@sendMessage
            }

//            dialogProvider.showSuccessAndWait("Feedback sent successfully")
//            navController.popBackStack()
        }
    }

    val closeCase: suspend () -> Unit = remember {
        closeCase@{
            dialogProvider.showProgressBar("Closing case")
            val (_, error) = safeRunIO {
                caseLogService.closeCase(reference)
            }
            dialogProvider.hideProgressBar()

            if (error != null) return@closeCase dialogProvider.showErrorAndWait(error)

            dialogProvider.showSuccessAndWait("Case closed")
            navController.popBackStack()
        }
    }

    Surface(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            CreditClubAppBar(
                title = title,
                modifier = Modifier.statusBarsPadding(),
                onBackPressed = onNavIconPressed,
            )
            if (isResolved) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .height(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Case has been resolved", modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { coroutineScope.launch { closeCase() } },
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = MaterialTheme.colors.secondary,
                            contentColor = MaterialTheme.colors.onSecondary,
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Close case")
                    }
                }
                Divider()
            }
            if (isClosed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Case closed", modifier = Modifier.fillMaxWidth())
                }
            }
            if (loading) {
                Loading(message = "Loading messages")
            } else {
                Messages(
                    messages = messageState.value,
                    navigateToProfile = navigateToProfile,
                    modifier = Modifier.weight(1f),
                    scrollState = scrollState
                )
            }
            if (!isClosed && !loading && errorMessage.isBlank()) {
                UserInput(
                    onMessageSent = { content ->
                        val newMessage = Feedback(
                            name = localStorage.agent!!.agentName!!,
                            message = content,
                            dateLogged = Instant.now(),
                            caseReference = reference,
                            isAgent = true,
                            fcmToken = fcmToken,
                        )
                        messageState.value = listOf(newMessage) + messageState.value
                        coroutineScope.launch { sendMessage(newMessage) }
                    },
                    onCameraClick = { imagePickerLauncher(CameraOnlyConfig()) },
                    scrollState = scrollState,
                    // Use navigationBarsWithImePadding(), to move the input panel above both the
                    // navigation bar, and on-screen keyboard (IME)
                    modifier = Modifier.navigationBarsWithImePadding(),
                )
            }
        }
    }
}

private const val ConversationTestTag = "ConversationTestTag"

@Composable
fun Messages(
    messages: List<Feedback>,
    navigateToProfile: (String) -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val localDateNow = remember { LocalDate.now() }
    val today = remember { localDateNow.format("dd/MM/uuuu") }
    val yesterday = remember { localDateNow.minusDays(1).format("dd/MM/uuuu") }
    val messageGroup = remember(messages) {
        messages.groupBy {
            when (val localDate = it.dateLogged.format("dd/MM/uuuu")) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> localDate
            }
        }
    }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier) {

        LazyColumn(
            state = scrollState,
            reverseLayout = true,
            modifier = Modifier
                .testTag(ConversationTestTag)
                .fillMaxWidth()
        ) {
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }

            for ((dayString, messageList) in messageGroup) {
                itemsIndexed(
                    messageList,
                    key = { _: Int, item: Feedback ->
                        item.id ?: "${item.name}/${item.dateLogged.epochSecond}"
                    },
                ) { index, content ->
                    val prevAuthor = messageList.getOrNull(index + 1)?.name
                    val nextAuthor = messageList.getOrNull(index - 1)?.name
                    val isFirstMessageByAuthor = prevAuthor != content.name
                    val isLastMessageByAuthor = nextAuthor != content.name

                    MessageItem(
                        onAuthorClick = {
                            navigateToProfile(content.name)
                        },
                        msg = content,
                        isUserMe = content.isAgent,
                        isFirstMessageByAuthor = isFirstMessageByAuthor,
                        isLastMessageByAuthor = isLastMessageByAuthor
                    )
                }

                item {
                    DayHeader(dayString)
                }
            }
        }
        // Jump to bottom button shows up when user scrolls past a threshold.
        // Convert to pixels:
        val jumpThreshold = with(LocalDensity.current) {
            JumpToBottomThreshold.toPx()
        }

        // Apply the threshold:
        val jumpToBottomButtonEnabled = scrollState.firstVisibleItemIndex > 2

        JumpToBottom(
            // Only show if the scroller is not at the bottom
            enabled = jumpToBottomButtonEnabled,
            onClicked = {
                scope.launch {
                    scrollState.animateScrollToItem(0)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun MessageItem(
    onAuthorClick: () -> Unit,
    msg: Feedback,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
) {
    // TODO: get image from msg.author
    val painter = if (isUserMe) {
        painterResource(id = R.drawable.ic_person)
    } else {
        painterResource(id = R.drawable.ic_person)
    }
    val borderColor = if (isUserMe) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.secondary
    }

    val spaceBetweenAuthors = if (isFirstMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier
    Row(modifier = spaceBetweenAuthors) {
        if (isFirstMessageByAuthor) {
            // Avatar
            Image(
                modifier = Modifier
                    .clickable(onClick = onAuthorClick)
                    .padding(horizontal = 16.dp)
                    .size(42.dp)
                    .border(1.5.dp, borderColor, CircleShape)
                    .border(3.dp, MaterialTheme.colors.surface, CircleShape)
                    .clip(CircleShape)
                    .align(Alignment.Top),
                painter = painter,
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        } else {
            // Space under avatar
            Spacer(modifier = Modifier.width(74.dp))
        }
        AuthorAndTextMessage(
            msg = msg,
            isFirstMessageByAuthor = isFirstMessageByAuthor,
            isLastMessageByAuthor = isLastMessageByAuthor,
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f)
        )
    }
}

@Composable
fun AuthorAndTextMessage(
    msg: Feedback,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (isFirstMessageByAuthor) {
            AuthorNameTimestamp(msg)
        }
        ChatItemBubble(msg, isLastMessageByAuthor)
        if (isLastMessageByAuthor) {
            // Last bubble before next author
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            // Between bubbles
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun AuthorNameTimestamp(msg: Feedback) {
    val authorMe = stringResource(R.string.author_me)

    // Combine author and timestamp for a11y.
    Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = if (msg.isAgent) authorMe else msg.name,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier
                .alignBy(LastBaseline)
                .paddingFrom(LastBaseline, after = 8.dp) // Space to 1st bubble
        )
        Spacer(modifier = Modifier.width(8.dp))
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = remember { msg.dateLogged.format("HH:mm") },
                style = MaterialTheme.typography.caption,
                modifier = Modifier.alignBy(LastBaseline)
            )
        }
    }
}

private val ChatBubbleShape = RoundedCornerShape(0.dp, 8.dp, 8.dp, 0.dp)
private val LastChatBubbleShape = RoundedCornerShape(0.dp, 8.dp, 8.dp, 8.dp)

@Composable
fun DayHeader(dayString: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(16.dp)
    ) {
        DayHeaderLine()
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = dayString,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.overline
            )
        }
        DayHeaderLine()
    }
}

@Composable
private fun RowScope.DayHeaderLine() {
    Divider(
        modifier = Modifier
            .weight(1f)
            .align(Alignment.CenterVertically),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    )
}

@Composable
fun ChatItemBubble(
    message: Feedback,
    lastMessageByAuthor: Boolean,
) {

    val backgroundBubbleColor =
        if (MaterialTheme.colors.isLight) {
            Color(0xFFF5F5F5)
        } else {
            MaterialTheme.colors.elevatedSurface(2.dp)
        }

    val bubbleShape = if (lastMessageByAuthor) LastChatBubbleShape else ChatBubbleShape
    Column {
        if (message.isAttachment) {
            message.message?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = backgroundBubbleColor, shape = bubbleShape) {
                    Image(
                        painter = rememberImagePainter(
                            data = it,
                            builder = {
                                crossfade(true)
                            },
                        ),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(160.dp),
                        contentDescription = stringResource(id = R.string.attached_image)
                    )
                }
            }
        } else {
            Surface(color = backgroundBubbleColor, shape = bubbleShape) {
                ClickableMessage(message = message)
            }
        }
    }
}

@Composable
fun ClickableMessage(message: Feedback) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(text = message.message ?: "")

    ClickableText(
        text = styledMessage,
        style = MaterialTheme.typography.body1.copy(color = LocalContentColor.current),
        modifier = Modifier.padding(8.dp),
        onClick = {
            styledMessage
                .getStringAnnotations(start = it, end = it)
                .firstOrNull()
                ?.let { annotation ->
                    when (annotation.tag) {
                        SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                        // TODO(yrezgui): Open profile screen when click PERSON tag
                        //  (e.g. @aliconors)
                        else -> Unit
                    }
                }
        }
    )
}

private val JumpToBottomThreshold = 56.dp

private fun ScrollState.atBottom(): Boolean = value == 0
