package com.cluster.conversation

import androidx.compose.runtime.mutableStateOf
import com.cluster.core.data.model.Feedback
import java.time.Instant

private val initialMessages = listOf(
    Feedback(
        id = 1,
        name = "me",
        message = "Compose newbie: I’ve scourged the internet for tutorials about async data loading " +
                "but haven’t found any good ones. What’s the recommended way to load async " +
                "data and emit composable widgets?",
        dateLogged = Instant.now(),
    ),
    Feedback(
        id = 2,
        name = "John Glenn",
        message = "Compose newbie as well, have you looked at the JetNews sample? Most blog posts end up " +
                "out of date pretty fast but this sample is always up to date and deals with async " +
                "data loading (it's faked but the same idea applies) \uD83D\uDC49" +
                "https://github.com/android/compose-samples/tree/master/JetNews",
        dateLogged = Instant.now(),
    ),
    Feedback(
        id = 3,
        name = "Taylor Brooks",
        message = "@aliconors Take a look at the `Flow.collectAsState()` APIs",
        dateLogged = Instant.now(),
    ),
    Feedback(
        id = 4,
        name = "Taylor Brooks",
        message = "You can use all the same stuff",
        dateLogged = Instant.now(),
    ),
    Feedback(
        id = 5,
        name = "me",
        message = "Thank you!",
        dateLogged = Instant.now(),
    ),
    Feedback(
        id = 6,
        name = "me",
        message = "Check it out!",
        dateLogged = Instant.now(),
    )
)

val exampleUiState = mutableStateOf(initialMessages)