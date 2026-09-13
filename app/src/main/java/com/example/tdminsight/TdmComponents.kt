package com.example.tdminsight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.exp
import kotlin.math.ln

enum class Method(val title: String, val subtitle: String) { PRE("Pre-dose", "Trough concentration"), POST("Post-dose", "Post-distribution sample"), PRE_POST("Pre + Post", "Two-point concentration") }
data class TdmInput(val patient: String, val weight: Double, val age: Double, val scr: Double, val dose: Double, val interval: Double, val pre: Double?, val post: Double?, val postTime: Double, val infusion: Double)
data class TdmResult(val method: Method, val ke: Double, val halfLife: Double, val vd: Double, val clearance: Double, val auc24: Double, val estimatedPeak: Double, val explanation: List<String>)

object TdmEngine {
    fun validate(i: TdmInput, method: Method): List<String> = buildList {
        if (i.patient.isBlank()) add("Enter a fictional patient/case name.")
        if (i.weight !in 20.0..250.0) add("Weight must be between 20 and 250 kg.")
        if (i.age !in 1.0..120.0) add("Age must be between 1 and 120 years.")
        if (i.scr !in 0.1..15.0) add("Serum creatinine must be between 0.1 and 15 mg/dL.")
        if (i.dose <= 0) add("Dose must be greater than zero.")
        if (i.interval !in 4.0..72.0) add("Dosing interval must be 4–72 hours.")
        if (i.infusion !in 0.25..4.0) add("Infusion duration must be 0.25–4 hours.")
        if (i.infusion >= i.interval) add("Infusion duration must be shorter than the dosing interval.")
        if (method != Method.POST && (i.pre == null || i.pre <= 0)) add("Enter a valid pre-dose concentration.")
        if (method != Method.PRE && (i.post == null || i.post <= 0)) add("Enter a valid post-dose concentration.")
        if (method != Method.PRE && i.postTime <= i.infusion) add("Post sample time must be after infusion completion.")
        if (method == Method.PRE_POST && (i.post ?: 0.0) <= (i.pre ?: 0.0)) add("For this educational two-point model, post-dose concentration must exceed pre-dose concentration.")
    }
    fun calculate(i: TdmInput, method: Method): TdmResult {
        val explanation = mutableListOf<String>()
        val ke = if (method == Method.PRE_POST) {
            val pre = i.pre ?: error("Pre-dose concentration is required.")
            val post = i.post ?: error("Post-dose concentration is required.")
            require(post > pre && i.postTime > 0.0) { "Concentrations and sampling time do not support a positive elimination slope." }
            explanation += "Ke = ln(Cpost / Cpre) ÷ time between the two observed samples."
            ln(post / pre) / i.postTime
        } else {
            explanation += "Ke is a population estimate from age, weight, and serum creatinine for educational demonstration only; it is not a validated clinical renal model."
            (0.00083 * (140.0 - i.age) * i.weight / (72.0 * i.scr) / 24.0).coerceIn(0.001, 1.0)
        }
        val half = ln(2.0) / ke
        val vd = if (method == Method.PRE) {
            explanation += "Vd uses a 0.7 L/kg population assumption because a trough alone cannot identify patient-specific Vd."
            (0.7 * i.weight).coerceAtLeast(1.0)
        } else {
            val post = i.post ?: error("Post-dose concentration is required.")
            val cmaxEnd = post * exp(ke * (i.postTime - i.infusion).coerceAtLeast(0.0))
            val infusionFactor = 1.0 - exp(-ke * i.infusion)
            require(cmaxEnd > 0.0 && infusionFactor > 0.0) { "Unable to estimate Vd from the supplied values." }
            explanation += "Vd is estimated from the post-dose concentration extrapolated to the end of infusion using a one-compartment intermittent-infusion model."
            (ke * i.infusion / (infusionFactor * cmaxEnd)).coerceAtLeast(1.0)
        }
        val clearance = vd * ke
        val auc = i.dose / clearance * 24.0 / i.interval
        val peak = if (method == Method.PRE) {
            val rate = i.dose / i.infusion
            rate / (vd * ke) * (1.0 - exp(-ke * i.infusion))
        } else {
            val post = i.post ?: error("Post-dose concentration is required.")
            post * exp(ke * (i.postTime - i.infusion).coerceAtLeast(0.0))
        }
        explanation += "Half-life = ln(2) ÷ Ke = ${fmt(half)} h."
        explanation += "Clearance = Vd × Ke = ${fmt(clearance)} L/h."
        explanation += "AUC24 estimate = dose ÷ clearance × (24 ÷ interval) = ${fmt(auc)} mg·h/L."
        explanation += "Educational one-compartment estimate only; it must not be used for clinical dose decisions."
        return TdmResult(method, ke, half, vd, clearance, auc, peak, explanation)
    }
}

fun fmt(value: Double) = "%.2f".format(value)

@Composable fun HomePage(onStart: () -> Unit) {
    Text("WELCOME TO", color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
    Text("TDM Insight", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, Line), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Teal), contentAlignment = Alignment.Center) { Icon(Icons.Default.MedicalServices, null, tint = Color.White, modifier = Modifier.size(27.dp)) }
                Column(Modifier.padding(start = 13.dp)) { Text("Vancomycin TDM", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("Clinical monitoring workspace", color = Slate, fontSize = 12.sp) }
                Spacer(Modifier.weight(1f)); Icon(Icons.Default.Lock, "Offline and private", tint = Teal, modifier = Modifier.size(19.dp))
            }
            Text("Calculate with confidence.", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Organize patient data, sampling times, and explainable PK estimates in one focused workflow.", color = Slate, fontSize = 14.sp, lineHeight = 20.sp)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Mint).padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Timeline, null, tint = Teal, modifier = Modifier.size(23.dp)); Column(Modifier.padding(start = 10.dp)) { Text("Guided in 3 simple steps", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("Method  •  Details  •  Results", color = Slate, fontSize = 11.sp) } }
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White)) { Text("Start new assessment", fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp)) }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { TrustCard("OFFLINE", "Private by design", Icons.Default.Shield, Modifier.weight(1f)); TrustCard("3 STEPS", "Guided workflow", Icons.Default.Timeline, Modifier.weight(1f)) }
    Section("Built for a careful review") {
        FeatureRow(Icons.Default.Person, "Structured patient profile", "Keep key context together")
        FeatureRow(Icons.Default.Science, "Method-aware inputs", "Only see fields you need")
        FeatureRow(Icons.Default.TrendingUp, "Explainable results", "Review each intermediate value")
    }
    InfoBanner("Academic prototype only", "Use fictional cases. Not a prescribing or treatment tool.")
}

@Composable fun TrustCard(label: String, caption: String, icon: ImageVector, modifier: Modifier = Modifier) { Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, Line), elevation = CardDefaults.cardElevation(0.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Teal, modifier = Modifier.size(22.dp)); Column(Modifier.padding(start = 10.dp)) { Text(label, color = Teal, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(caption, color = Slate, fontSize = 11.sp) } } } }
@Composable fun FeatureRow(icon: ImageVector, title: String, caption: String) { Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(36.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Teal, modifier = Modifier.size(19.dp)) }; Column(Modifier.padding(start = 12.dp)) { Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Text(caption, color = Slate, fontSize = 12.sp) } } }

@Composable fun MethodPage(method: Method, onMethod: (Method) -> Unit, onBack: () -> Unit, onContinue: () -> Unit) { StepIndicator(1); PageTitle("Choose a sampling method", "Select the workflow that matches your available concentration data."); Section("Sampling workflow") { Method.entries.forEach { m -> MethodChoice(m, method == m) { onMethod(m) } } }; InfoBanner("Tip", "Use Pre + Post when both concentrations are available for a two-point estimate."); ActionRow("Continue", onBack, onContinue); Disclaimer() }

@Composable fun InputPage(method: Method, patient: String, setPatient: (String) -> Unit, weight: String, setWeight: (String) -> Unit, age: String, setAge: (String) -> Unit, scr: String, setScr: (String) -> Unit, dose: String, setDose: (String) -> Unit, interval: String, setInterval: (String) -> Unit, infusion: String, setInfusion: (String) -> Unit, pre: String, setPre: (String) -> Unit, post: String, setPost: (String) -> Unit, postTime: String, setPostTime: (String) -> Unit, errors: List<String>, onBack: () -> Unit, onCalculate: () -> Unit) {
    StepIndicator(2); PageTitle("Enter assessment data", "Complete the required fields for this fictional case.")
    Section("Patient profile") { Field("Case name", patient, setPatient, KeyboardType.Text, "e.g. Demo patient", Icons.Default.Person); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Box(Modifier.weight(1f)) { Field("Weight", weight, setWeight, KeyboardType.Decimal, "kg") }; Box(Modifier.weight(1f)) { Field("Age", age, setAge, KeyboardType.Decimal, "years") } }; Field("Serum creatinine", scr, setScr, KeyboardType.Decimal, "mg/dL") }
    Section("Medication regimen") { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Box(Modifier.weight(1f)) { Field("Dose", dose, setDose, KeyboardType.Decimal, "mg") }; Box(Modifier.weight(1f)) { Field("Interval", interval, setInterval, KeyboardType.Decimal, "hours") } }; Field("Infusion duration", infusion, setInfusion, KeyboardType.Decimal, "hours", Icons.Default.AccessTime) }
    Section("Sampling data • ${method.title}") { if (method != Method.POST) Field("Pre-dose concentration", pre, setPre, KeyboardType.Decimal, "mg/L", Icons.Default.Science); if (method != Method.PRE) { Field("Post-dose concentration", post, setPost, KeyboardType.Decimal, "mg/L", Icons.Default.Science); Field("Post sample time from dose start", postTime, setPostTime, KeyboardType.Decimal, "hours", Icons.Default.AccessTime) } }
    if (errors.isNotEmpty()) ErrorBox(errors)
    ActionRow("Calculate results", onBack, onCalculate); Disclaimer()
}

@Composable fun PageTitle(title: String, subtitle: String) { Text(title, color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = Slate, fontSize = 14.sp, lineHeight = 20.sp) }
@Composable fun StepIndicator(step: Int) { Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { listOf("Method", "Details", "Results").forEachIndexed { index, label -> val active = index <= step; Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) { Box(Modifier.size(28.dp).clip(CircleShape).background(if (active) Teal else Mist), contentAlignment = Alignment.Center) { if (index < step) Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp)) else Text("${index + 1}", color = if (active) Color.White else Slate, fontSize = 12.sp, fontWeight = FontWeight.Bold) }; Text(label, color = if (active) Teal else Slate, fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal) }; if (index < 2) HorizontalDivider(Modifier.weight(.45f).padding(bottom = 18.dp), color = if (index < step) Teal else Line) } } }
@Composable fun MethodChoice(method: Method, selected: Boolean, onClick: () -> Unit) { val icon = when (method) { Method.PRE -> Icons.Default.Science; Method.POST -> Icons.Default.AccessTime; Method.PRE_POST -> Icons.Default.Timeline }; Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(if (selected) Mint else Color.White), border = BorderStroke(1.dp, if (selected) Teal else Line), elevation = CardDefaults.cardElevation(0.dp)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(if (selected) Teal else Mist), contentAlignment = Alignment.Center) { Icon(icon, null, tint = if (selected) Color.White else Teal) }; Column(Modifier.padding(start = 13.dp).weight(1f)) { Text(method.title, fontWeight = FontWeight.Bold, color = Ink); Text(method.subtitle, color = Slate, fontSize = 12.sp) }; if (selected) Icon(Icons.Default.CheckCircle, "Selected", tint = Teal) } } }
@Composable fun Section(title: String, content: @Composable ColumnScope.() -> Unit) { Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, Line), elevation = CardDefaults.cardElevation(0.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) { Text(title, fontWeight = FontWeight.Bold, color = Ink, fontSize = 16.sp); content() } } }
@Composable fun Field(label: String, value: String, onChange: (String) -> Unit, type: KeyboardType, placeholder: String, icon: ImageVector? = null) { OutlinedTextField(value, onChange, label = { Text(label) }, placeholder = { Text(placeholder) }, leadingIcon = icon?.let { { Icon(it, null, tint = Teal) } }, keyboardOptions = KeyboardOptions(keyboardType = type), singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Teal, focusedLabelColor = Teal, cursorColor = Teal)) }
@Composable fun ActionRow(primary: String, onBack: () -> Unit, onPrimary: () -> Unit) { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedButton(onClick = onBack, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(6.dp)); Text("Back") }; Button(onClick = onPrimary, Modifier.weight(1.45f).height(50.dp), shape = RoundedCornerShape(14.dp)) { Text(primary, fontWeight = FontWeight.Bold); Spacer(Modifier.width(6.dp)); Icon(if (primary.startsWith("Calculate")) Icons.Default.Calculate else Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp)) } } }
@Composable fun ErrorBox(errors: List<String>) { Card(colors = CardDefaults.cardColors(Color(0xFFFFF1F0)), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFF4C7C3))) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text("Check the highlighted information", fontWeight = FontWeight.Bold, color = Color(0xFF9B2C2C)); errors.forEach { Text("• $it", color = Color(0xFF9B2C2C), fontSize = 13.sp) } } } }

@Composable fun ResultScreen(r: TdmResult, onReset: () -> Unit) { StepIndicator(3); PageTitle("Assessment results", "${r.method.title} workflow • educational estimate"); Card(colors = CardDefaults.cardColors(Navy), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.TrendingUp, null, tint = Aqua); Text("PRIMARY RESULT", color = Aqua, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) }; Text("Estimated AUC24", color = Color.White.copy(.75f), fontSize = 14.sp); Text("${fmt(r.auc24)}", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold); Text("mg·h/L", color = Aqua, fontSize = 14.sp); Text("For review only — not a dosing recommendation", color = Color.White.copy(.65f), fontSize = 12.sp) } }
    Section("Pharmacokinetic parameters") { ResultRow("Elimination rate constant (Ke)", "${fmt(r.ke)} /h"); ResultRow("Elimination half-life", "${fmt(r.halfLife)} h"); ResultRow("Volume of distribution", "${fmt(r.vd)} L"); ResultRow("Clearance", "${fmt(r.clearance)} L/h"); ResultRow("Estimated peak", "${fmt(r.estimatedPeak)} mg/L") }
    var expanded by remember { mutableStateOf(false) }; Section("Calculation explanation") { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null, tint = Teal, modifier = Modifier.size(19.dp)); Text("Transparent intermediate values", color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp)) }; TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide calculation steps" else "Show calculation steps"); Icon(Icons.Default.ExpandMore, null) }; if (expanded) r.explanation.forEachIndexed { n, s -> Text("${n + 1}. $s", fontSize = 13.sp, color = Slate, lineHeight = 19.sp) } }
    Button(onClick = onReset, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Start new assessment", fontWeight = FontWeight.Bold) }; Disclaimer()
}
@Composable fun ResultRow(label: String, value: String) { Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Slate, fontSize = 13.sp); Text(value, fontWeight = FontWeight.Bold, color = Ink, fontSize = 13.sp) } }
@Composable fun InfoBanner(title: String, message: String) { Card(colors = CardDefaults.cardColors(Mint), shape = RoundedCornerShape(16.dp)) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.Top) { Icon(Icons.Default.Info, null, tint = Teal, modifier = Modifier.size(19.dp)); Column(Modifier.padding(start = 9.dp)) { Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(message, color = Slate, fontSize = 12.sp, lineHeight = 17.sp) } } } }
@Composable fun Disclaimer() { Text("Academic prototype only. Not clinically validated and must not be used for prescribing, diagnosis, or treatment decisions.", fontSize = 11.sp, color = Slate, lineHeight = 15.sp) }
