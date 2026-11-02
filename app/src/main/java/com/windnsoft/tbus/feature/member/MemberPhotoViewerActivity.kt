package com.windnsoft.tbus.feature.member

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.windnsoft.tbus.designsystem.theme.TBusTheme
import soup.compose.photo.ExperimentalPhotoApi
import soup.compose.photo.PhotoBox
import soup.compose.photo.rememberPhotoState

class MemberPhotoViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
        )

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL).orEmpty()

        setContent {
            TBusTheme {
                MemberPhotoViewerScreen(
                    imageUrl = imageUrl,
                    onCloseClick = { finish() },
                )
            }
        }
    }

    companion object {
        private const val EXTRA_IMAGE_URL = "extra_image_url"

        fun createIntent(context: Context, imageUrl: String): Intent {
            return Intent(context, MemberPhotoViewerActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URL, imageUrl)
            }
        }
    }
}

@OptIn(ExperimentalPhotoApi::class)
@Composable
fun MemberPhotoViewerScreen(
    imageUrl: String,
    onCloseClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl.isNotBlank()) {
            GlideImage(
                imageModel = { imageUrl },
                modifier = Modifier.fillMaxSize(),
                requestOptions = {
                    RequestOptions()
                        .override(2048, 2048)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                },
                imageOptions = ImageOptions(
                    alignment = Alignment.Center,
                    requestSize = IntSize(2048, 2048),
                ),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                },
                success = { _, painter ->
                    val photoState = rememberPhotoState()
                    photoState.setPhotoIntrinsicSize(painter.intrinsicSize)
                    PhotoBox(
                        state = photoState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                },
            )
        }

        // 우측 상단 X 닫기 아이콘
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 12.dp)
                .size(44.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
