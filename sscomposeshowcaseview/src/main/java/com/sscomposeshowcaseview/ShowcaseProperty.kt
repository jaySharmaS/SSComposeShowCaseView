package com.sscomposeshowcaseview

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat

data class ShowcaseProperty(
    val index: Int,
    val coordinates: LayoutCoordinates,
    val showCaseType: ShowcaseType = ShowcaseType.SIMPLE_ROUNDED,
    val blurOpacity: Float = 0.8f,
    val showcaseDelay: Long = 5000,
    val padding: Dp = 20.dp,
    val content: @Composable ShowcaseScope.() -> Unit
) {

    companion object {
        operator fun invoke(
            index: Int,
            coordinates: LayoutCoordinates,
            title: String,
            titleColor: Color = Color.White,
            subTitle: String,
            subTitleColor: Color = Color.White,
            showCaseType: ShowcaseType = ShowcaseType.SIMPLE_ROUNDED,
            blurOpacity: Float = 0.8f,
            showcaseDelay: Long = 2000,
            padding: Dp = 20.dp,
        ): ShowcaseProperty {
            return ShowcaseProperty(
                index = index,
                coordinates = coordinates,
                showCaseType = showCaseType,
                blurOpacity = blurOpacity,
                showcaseDelay = showcaseDelay,
                padding = padding
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    Text(
                        text = subTitle,
                        fontSize = 14.sp,
                        color = subTitleColor
                    )
                }
            }
        }

        operator fun invoke(
            index: Int,
            coordinates: LayoutCoordinates,
            title: HtmlText,
            titleColor: Color = Color.White,
            subTitle: String,
            subTitleColor: Color = Color.White,
            showCaseType: ShowcaseType = ShowcaseType.SIMPLE_ROUNDED,
            blurOpacity: Float = 0.8f,
            showcaseDelay: Long = 2000,
            padding: Dp = 20.dp,
        ): ShowcaseProperty {
            return ShowcaseProperty(
                index = index,
                coordinates = coordinates,
                showCaseType = showCaseType,
                blurOpacity = blurOpacity,
                showcaseDelay = showcaseDelay,
                padding = padding
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = HtmlCompat.fromHtml(
                            title.text,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        ).toAnnotatedString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    Text(
                        text = subTitle,
                        fontSize = 14.sp,
                        color = subTitleColor
                    )
                }
            }
        }

        operator fun invoke(
            index: Int,
            coordinates: LayoutCoordinates,
            title: String,
            titleColor: Color = Color.White,
            subTitle: HtmlText,
            subTitleColor: Color = Color.White,
            showCaseType: ShowcaseType = ShowcaseType.SIMPLE_ROUNDED,
            blurOpacity: Float = 0.8f,
            showcaseDelay: Long = 2000,
            padding: Dp = 20.dp,
        ): ShowcaseProperty {
            return ShowcaseProperty(
                index = index,
                coordinates = coordinates,
                showCaseType = showCaseType,
                blurOpacity = blurOpacity,
                showcaseDelay = showcaseDelay,
                padding = padding
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    Text(
                        text = HtmlCompat.fromHtml(
                            subTitle.text,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        ).toAnnotatedString(),
                        fontSize = 14.sp,
                        color = subTitleColor
                    )
                }
            }
        }

        operator fun invoke(
            index: Int,
            coordinates: LayoutCoordinates,
            title: HtmlText,
            titleColor: Color = Color.White,
            subTitle: HtmlText,
            subTitleColor: Color = Color.White,
            showCaseType: ShowcaseType = ShowcaseType.SIMPLE_ROUNDED,
            blurOpacity: Float = 0.8f,
            showcaseDelay: Long = 2000,
            padding: Dp = 20.dp,
        ): ShowcaseProperty {
            return ShowcaseProperty(
                index = index,
                coordinates = coordinates,
                showCaseType = showCaseType,
                blurOpacity = blurOpacity,
                showcaseDelay = showcaseDelay,
                padding = padding
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = HtmlCompat.fromHtml(
                            title.text,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        ).toAnnotatedString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    Text(
                        text = HtmlCompat.fromHtml(
                            subTitle.text,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        ).toAnnotatedString(),
                        fontSize = 14.sp,
                        color = subTitleColor
                    )
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
                        Typeface.BOLD -> addStyle(
                            SpanStyle(fontWeight = FontWeight.Bold),
                            start,
                            end
                        )

                        Typeface.ITALIC -> addStyle(
                            SpanStyle(fontStyle = FontStyle.Italic),
                            start,
                            end
                        )

                        Typeface.BOLD_ITALIC -> addStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
                            ), start, end
                        )
                    }

                    is UnderlineSpan -> addStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline),
                        start,
                        end
                    )

                    is ForegroundColorSpan -> addStyle(
                        SpanStyle(color = Color(span.foregroundColor)),
                        start,
                        end
                    )
                }
            }
        }
    }
}
