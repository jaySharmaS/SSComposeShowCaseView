package com.example.sscomposeshowcaseview

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import com.sscomposeshowcaseview.ShowCaseTarget
import com.sscomposeshowcaseview.ShowcaseProperty
import com.sscomposeshowcaseview.ShowcaseType
import com.sscomposeshowcaseview.TestOverlap
import com.sscomposeshowcaseview.TestPointer2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            /*Box(modifier = Modifier.fillMaxSize()) {
                TestPointer2()
            }*/
            //ShowcaseExample()
            TestOverlap(content =  {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "More options",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Click here to see options",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Button(onClick = { }) {
                        Text(text = "Skip All")
                    }
                }
            })
        }
    }
}

@Composable
private fun ShowcaseExample() {
    val targets = remember { mutableStateMapOf<String, ShowcaseProperty>() }
    val context = LocalContext.current

    Scaffold(topBar = { TopAppBar(target = targets) }) {
        Column(modifier = Modifier.padding(it)) {
            Posts(target = targets)
        }
    }

    ShowCaseTarget(targets = targets, isEnableAutoShowCase = true, key = "Dashboard") {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Thank you! Intro Completed", Toast.LENGTH_SHORT)
                .show()
        }
    }
}

@Composable
private fun TopAppBar(target: SnapshotStateMap<String, ShowcaseProperty>) {
    TopAppBar(title = { Text(text = "SSShowcaseView", color = Color.White)},
        backgroundColor = colorResource(id = R.color.purple_500)
    )
}

@Composable
private fun Posts(target: SnapshotStateMap<String, ShowcaseProperty>) {
    LazyColumn {
        items(items = Data.userList) { item ->
            PostItem(post = item, target = target)
        }
    }
}

@Composable
fun PostItem(post: Item, target: SnapshotStateMap<String, ShowcaseProperty>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        UserProfile(post = post, target = target)
        Divider(Modifier.fillMaxWidth(), thickness = 1.dp)
        UserPost(post = post, target = target)
    }
}

@Composable
fun UserProfile(post: Item, target: SnapshotStateMap<String, ShowcaseProperty>) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.onGloballyPositioned {
            target["profile"] = ShowcaseProperty(
                index = 6,
                coordinates = it,
                showCaseType = ShowcaseType.ANIMATED_RECTANGLE
            ) {
                ShowCaseDescription(
                    title = "Profile",
                    subTitle = "User Profile with name and picture",
                    onSkip = onSkipShowcase
                )
            }
        }) {
        Image(
            painter = painterResource(id = post.profilePic),
            contentDescription = "User profile pic",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(10.dp)
                .clip(CircleShape)
                .size(30.dp)
        )
        Text(text = post.name, modifier = Modifier.weight(1f))
        Image(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More icon",
            modifier = Modifier
                .padding(10.dp)
                .onGloballyPositioned {
                    target["more"] = ShowcaseProperty(
                        index = 1,
                        coordinates = it,
                        showCaseType = ShowcaseType.POINTER,
                        showcaseDelay = 5000
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "More options",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Click here to see options",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Button(onClick = this@ShowcaseProperty.onSkipSequence) {
                                Text(text = "Skip All")
                            }
                        }
                    }
                }
        )
    }
}

@Composable
fun UserPost(post: Item, target: SnapshotStateMap<String, ShowcaseProperty>) {
    val isLikeClicked = remember { mutableStateOf(true) }
    //val focusRequester = remember { FocusRequester() }
    Column(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = post.profilePic),
            contentDescription = "User post",
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .onGloballyPositioned {
                    target["post_image"] = ShowcaseProperty(
                        index = 7,
                        coordinates = it,
                        showCaseType = ShowcaseType.POINTER,
                        showcaseDelay = 15000
                    ) {
                        ShowCaseDescription(
                            title = "Post Image",
                            subTitle = "Click here to add comment on post and it has some very long text to test the feature how it works. Hopefully it works as intended nothing complicates. Lets see how it goes.",
                            onSkip = onSkipShowcase
                        )
                    }
                },
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isLikeClicked.value = !isLikeClicked.value }) {
                    Icon(
                        imageVector = if (isLikeClicked.value) Icons.Default.FavoriteBorder else Icons.Filled.Favorite,
                        contentDescription = "Fav",
                        modifier = Modifier
                            //.focusRequester(focusRequester)
                            //.focusable()
                            .size(30.dp)
                            .onGloballyPositioned {
                                target["like"] = ShowcaseProperty(
                                    index = 2,
                                    coordinates = it,
                                    showCaseType = ShowcaseType.ANIMATED_ROUNDED,
                                    showcaseDelay = 5000
                                ) {
                                    //focusRequester.requestFocus()
                                    ShowCaseDescription(
                                        title = "LIke Post",
                                        subTitle = "Click here to like post",
                                        onSkip = onSkipShowcase
                                    )
                                }
                            }
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "add",
                        modifier = Modifier
                            .size(28.dp)
                            .onGloballyPositioned {
                                target["comment"] = ShowcaseProperty(
                                    index = 3,
                                    coordinates = it,
                                    showCaseType = ShowcaseType.ANIMATED_RECTANGLE,
                                    showcaseDelay = 1000
                                ) {
                                    ShowCaseDescription(
                                        title = "Comment button",
                                        subTitle = "Click here to add comment on post and it has some very long text to test the feature how it works. Hopefully it works as intended nothing complicates. Lets see how it goes.",
                                        onSkip = onSkipShowcase
                                    )
                                }
                            }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        modifier = Modifier
                            .size(23.dp)
                            .onGloballyPositioned {
                                target["share"] = ShowcaseProperty(
                                    index = 4,
                                    coordinates = it,
                                    showCaseType = ShowcaseType.ANIMATED_RECTANGLE,
                                ) {
                                    ShowCaseDescription(
                                        title = "Share button",
                                        subTitle = "Click here to Share post with others",
                                        onSkip = onSkipShowcase
                                    )
                                }
                            }
                    )
                }
            }
            IconButton(modifier = Modifier
                .align(Alignment.CenterEnd),
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "Save",
                    modifier = Modifier
                        .size(25.dp)
                        .onGloballyPositioned {
                            target["save"] = ShowcaseProperty(
                                index = 5,
                                coordinates = it,
                                showCaseType = ShowcaseType.ANIMATED_RECTANGLE
                            ) {
                                ShowCaseDescription(
                                    title = "Save button",
                                    subTitle = HtmlCompat
                                        .fromHtml(
                                            "Click <i>here</i> to save <b>post</b>",
                                            HtmlCompat.FROM_HTML_MODE_COMPACT
                                        )
                                        .toAnnotatedString(),
                                    onSkip = onSkipShowcase
                                )
                            }
                        }
                )
            }
        }
    }
}

@Composable
fun ShowCaseDescription(
    title: String,
    subTitle: String,
    onSkip: () -> Unit = { }
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = subTitle,
            fontSize = 14.sp,
            color = Color.White
        )
        Button(onClick = onSkip) {
            Text(text = "Skip")
        }
    }
}

@Composable
fun ShowCaseDescription(
    title: String,
    subTitle: AnnotatedString,
    onSkip: () -> Unit = { }
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = subTitle,
            fontSize = 14.sp,
            color = Color.White
        )
        Button(onClick = onSkip) {
            Text(text = "Skip")
        }
    }
}

/**
 * Converts a [Spanned] into an [AnnotatedString] trying to keep as much formatting as possible.
 *
 * Currently supports `bold`, `italic`, `underline` and `color`.
 */
private fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    val spanned = this@toAnnotatedString
    append(spanned.toString())
    getSpans(0, spanned.length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)
        when (span) {
            is StyleSpan -> when (span.style) {
                Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
            }
            is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
            is ForegroundColorSpan -> addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
        }
    }
}

@Preview
@Composable
fun MainActivityPreview() {
    Surface {
        ShowcaseExample()
    }
}
