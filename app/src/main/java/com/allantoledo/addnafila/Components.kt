package com.allantoledo.addnafila

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.allantoledo.addnafila.models.Track


@Composable
fun LoadingTextField(
    onValueChange: (String) -> Unit,
    value: String,
    loadingColor: Color,
    showSearchIndication: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.blue_gray_800),
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp)),
    ) {
        Column(Modifier.fillMaxWidth()) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = colorResource(R.color.blue_gray_800),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    textColor = Color.White
                ),
                placeholder = {
                    Text(
                        "Digite aqui".uppercase(),
                        color = loadingColor,
                        fontWeight = FontWeight.Thin,
                        fontSize = 18.sp,
                        letterSpacing = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .alpha(0.3f)
                            .fillMaxWidth()
                    )
                }
            )
            if (showSearchIndication)
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    backgroundColor = Color.Transparent,
                    color = colorResource(R.color.green_light)
                )
        }
    }
}


@ExperimentalMaterialApi
@Composable
fun SongCard(
    song: Track,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = colorResource(R.color.blue_gray_800),
        onClick = onClick
    ) {
        Row(Modifier.padding(8.dp)) {
            if (song.imageURI != null)
                Image(
                    painter = rememberImagePainter(song.imageURI),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            Column(
                modifier = Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 0.dp
                )
            ) {
                Text(
                    song.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    song.artists.joinToString(", "),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.alpha(0.8f)
                )
            }
        }

    }
}

@Composable
fun ConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    message: String
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        backgroundColor = colorResource(R.color.blue_gray_800),
        title = {
            Text(
                "Certeza?",
                color = colorResource(R.color.green_light),
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        },
        text = {
            Text(
                message,
                color = colorResource(R.color.white),
                fontSize = 18.sp,
                modifier = Modifier.alpha(0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(R.color.green_light),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Text(
                    "Sim",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        }
    )
}

@Composable
fun SuccessDialog(
    onDismissRequest: () -> Unit,
    message: String
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        backgroundColor = colorResource(R.color.blue_gray_800),
        title = {
            Text(
                "Pronto!",
                color = colorResource(R.color.green_light),
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        },
        text = {
            Text(
                message,
                color = colorResource(R.color.white),
                fontSize = 18.sp,
                modifier = Modifier.alpha(0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(R.color.green_light),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Text(
                    "Blz",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        }
    )
}

@Composable
fun ErrorDialog(
    onDismissRequest: () -> Unit,
    errorMessage: String,
    onAction: () -> Unit,
    actionText: String,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        backgroundColor = colorResource(R.color.blue_gray_800),
        title = {
            Text(
                "Opss...!",
                color = colorResource(R.color.green_light),
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        },
        text = {
            Text(
                errorMessage,
                color = colorResource(R.color.white),
                fontSize = 18.sp,
                modifier = Modifier.alpha(0.8f)
            )
        },
        buttons = {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Text(
                    actionText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = colorResource(R.color.green_light)
                )
            }
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(R.color.green_light),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Text(
                    "Tentar novamente",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        }
    )

}