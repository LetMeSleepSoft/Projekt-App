package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.dieschnittstelle.mobile.android.kotlin.skeleton.R

val Monserrat = FontFamily(
    Font(R.font.montserrat_regular),
    Font(R.font.montserrat_bold)
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Monserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp
    ),
    displayMedium = TextStyle(
        fontFamily = Monserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Monserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
)