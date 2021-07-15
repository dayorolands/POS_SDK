package com.appzonegroup.app.fasttrack.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.pos.printer.*
import com.creditclub.pos.rememberPosPrinter
import com.creditclub.ui.AppButton
import com.creditclub.ui.CreditClubAppBar
import com.creditclub.ui.ErrorMessage
import kotlinx.coroutines.launch

@Composable
fun ReceiptDetails(
    navController: NavController,
    printJob: PrintJob,
    showAppBar: Boolean = true,
    onBackPressed: () -> Unit = { navController.popBackStack() },
) {
    val coroutineScope = rememberCoroutineScope()
    val posPrinter by rememberPosPrinter()
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
    ) {
        if (showAppBar) {
            CreditClubAppBar(title = "", onBackPressed = onBackPressed)
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Spacer(modifier = Modifier.padding(top = 16.dp))
            }
            for (node in printJob.nodes) {
                when (node) {
                    is TextNode -> item {
                        Text(
                            text = node.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 26.dp,
                                    end = 26.dp,
                                    bottom = (node.walkPaperAfterPrint + 5).dp,
                                ),
                            fontWeight = if (node.isBold) FontWeight.Bold else null,
                            fontSize = (node.wordFont - 4).sp,
                            textAlign = when (node.align) {
                                com.creditclub.pos.printer.Alignment.LEFT -> TextAlign.Start
                                com.creditclub.pos.printer.Alignment.MIDDLE -> TextAlign.Center
                                com.creditclub.pos.printer.Alignment.RIGHT -> TextAlign.End
                            },
                        )
                    }
                    is ImageNode -> item {
                        Image(
                            painter = painterResource(id = node.drawable),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .height(80.dp)
                                .padding(bottom = node.walkPaperAfterPrint.dp),
                            alignment = when (node.align) {
                                com.creditclub.pos.printer.Alignment.LEFT -> Alignment.BottomStart
                                com.creditclub.pos.printer.Alignment.MIDDLE -> Alignment.BottomCenter
                                com.creditclub.pos.printer.Alignment.RIGHT -> Alignment.BottomEnd
                            }
                        )
                    }
                    is WalkPaper -> item {
                        Spacer(modifier = Modifier.size(node.walkPaperAfterPrint.dp))
                    }
                    else -> {
                    }
                }
            }
        }

        ErrorMessage(content = errorMessage)

        AppButton(
            modifier = Modifier.padding(horizontal = 10.dp),
            onClick = {
                coroutineScope.launch {
                    val status = posPrinter.print(printJob)
                    errorMessage = if (status != PrinterStatus.READY) status.message else ""
                }
            },
        ) {
            Text(text = if (Platform.isPOS) "PRINT RECEIPT" else "SHARE RECEIPT")
        }
    }
}