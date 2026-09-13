package com.example.tdminsight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class AppPage { HOME, METHOD, INPUT, RESULT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    var page by remember { mutableStateOf(AppPage.HOME) }
    var method by remember { mutableStateOf(Method.PRE) }
    var sex by remember { mutableStateOf(Sex.MALE) }
    var result by remember { mutableStateOf<TdmResult?>(null) }
    var errors by remember { mutableStateOf(emptyList<String>()) }
    var patient by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var scr by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var interval by remember { mutableStateOf("") }
    var infusion by remember { mutableStateOf("") }
    var pre by remember { mutableStateOf("") }
    var post by remember { mutableStateOf("") }
    var postTime by remember { mutableStateOf("") }

    val input = TdmInput(patient, sex, weight.toDoubleOrNull() ?: -1.0, age.toDoubleOrNull() ?: -1.0, scr.toDoubleOrNull() ?: -1.0, dose.toDoubleOrNull() ?: -1.0, interval.toDoubleOrNull() ?: -1.0, pre.toDoubleOrNull(), post.toDoubleOrNull(), postTime.toDoubleOrNull() ?: 0.0, infusion.toDoubleOrNull() ?: -1.0)

    Scaffold(
        containerColor = Pale,
        topBar = {
            TopAppBar(
                title = { Column { Text("TDM Insight", fontWeight = FontWeight.Bold, color = Ink, fontSize = 18.sp); Text("Vancomycin monitoring", fontSize = 11.sp, color = Slate) } },
                navigationIcon = { Icon(Icons.Default.MedicalServices, "TDM Insight", tint = Teal, modifier = Modifier.padding(start = 18.dp, end = 10.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Pale)
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Pale).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (page) {
                AppPage.HOME -> HomePage { page = AppPage.METHOD }
                AppPage.METHOD -> MethodPage(method, { method = it }, { page = AppPage.HOME }, { page = AppPage.INPUT })
                AppPage.INPUT -> InputPage(method, patient, { patient = it }, sex, { sex = it }, weight, { weight = it }, age, { age = it }, scr, { scr = it }, dose, { dose = it }, interval, { interval = it }, infusion, { infusion = it }, pre, { pre = it }, post, { post = it }, postTime, { postTime = it }, errors, { page = AppPage.METHOD }) {
                    val validationErrors = TdmEngine.validate(input.copy(post = if (method == Method.PRE) null else input.post), method)
                    errors = validationErrors
                    if (validationErrors.isEmpty()) { result = TdmEngine.calculate(input, method); page = AppPage.RESULT }
                }
                AppPage.RESULT -> result?.let { ResultScreen(it) { page = AppPage.HOME; result = null; errors = emptyList() } }
            }
        }
    }
}
