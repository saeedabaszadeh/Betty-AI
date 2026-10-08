package com.bettyai.assistant.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenPermissions: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val s = state.settings

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---- General ----
            item {
                SettingsSection("عمومی") {
                    ToggleRow(
                        title = "فعال بودن بتی",
                        subtitle = "وقتی غیرفعال باشد، بتی به هیچ درخواستی پاسخ نمی‌دهد",
                        checked = s.bettyEnabled,
                        onCheckedChange = viewModel::setBettyEnabled
                    )
                    Divider()
                    ToggleRow(
                        title = "پاسخ صوتی",
                        subtitle = "بتی پاسخ‌ها را با صدا می‌خواند",
                        checked = s.voiceEnabled,
                        onCheckedChange = viewModel::setVoiceEnabled
                    )
                    Divider()
                    var name by remember(s.userDisplayName) { mutableStateOf(s.userDisplayName) }
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.setUserDisplayName(it)
                        },
                        label = { Text("نام شما") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }

            // ---- Voice & Language ----
            item {
                SettingsSection("صدا و زبان") {
                    Text(
                        "زبان",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LanguageChip("فارسی", "fa-IR", s.languageTag, viewModel::setLanguage)
                        LanguageChip("English", "en-US", s.languageTag, viewModel::setLanguage)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "صدای گفتار (TTS)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    if (state.ttsVoices.isEmpty()) {
                        Text(
                            "صدایی یافت نشد؛ موتور گفتار اندروید را نصب کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        VoiceSelector(
                            voices = state.ttsVoices.map { it.name },
                            selected = s.ttsVoiceName,
                            onSelect = viewModel::setTtsVoice
                        )
                    }
                }
            }

            // ---- Wake word ----
            item {
                SettingsSection("کلمهٔ بیدارباش") {
                    ToggleRow(
                        title = "کلمهٔ بیدارباش «سلام بتی»",
                        subtitle = if (state.wakeWordSupported) {
                            "با گفتن «سلام بتی» بتی فعال می‌شود"
                        } else {
                            "در نسخهٔ فعلی پشتیبانی نمی‌شود؛ معماری آن آماده است"
                        },
                        checked = s.wakeWordEnabled && state.wakeWordSupported,
                        enabled = state.wakeWordSupported,
                        onCheckedChange = viewModel::setWakeWordEnabled
                    )
                }
            }

            // ---- AI ----
            item {
                SettingsSection("هوش مصنوعی") {
                    Text(
                        "ارائه‌دهنده",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProviderChip("OpenAI-Compatible", "openai_compatible", s.aiProviderId, viewModel::setAiProvider)
                    }
                    Spacer(Modifier.height(12.dp))
                    var baseUrl by remember(s.aiBaseUrl) { mutableStateOf(s.aiBaseUrl) }
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = {
                            baseUrl = it
                            viewModel.setAiBaseUrl(it)
                        },
                        label = { Text("آدرس پایه (Base URL)") },
                        placeholder = { Text("https://api.openai.com/v1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    var model by remember(s.aiModel) { mutableStateOf(s.aiModel) }
                    OutlinedTextField(
                        value = model,
                        onValueChange = {
                            model = it
                            viewModel.setAiModel(it)
                        },
                        label = { Text("مدل") },
                        placeholder = { Text("gpt-4o-mini") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    ApiKeyField(
                        hasKey = s.hasApiKey,
                        onSave = viewModel::setApiKey,
                        onClear = viewModel::clearApiKey
                    )
                }
            }

            // ---- Skills ----
            item {
                SettingsSection("قابلیت‌ها") {
                    if (state.skills.isEmpty()) {
                        Text(
                            "قابلیتی ثبت نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    state.skills.forEachIndexed { index, skill ->
                        if (index > 0) Divider()
                        ToggleRow(
                            title = skill.name,
                            subtitle = skill.description,
                            checked = skill.enabled,
                            onCheckedChange = { viewModel.setSkillEnabled(skill.id, it) }
                        )
                    }
                }
            }

            // ---- Permissions & privacy ----
            item {
                SettingsSection("دسترسی‌ها و حریم خصوصی") {
                    NavRow(
                        title = "وضعیت دسترسی‌ها",
                        subtitle = "مدیریت مجوزهای میکروفون و اعلان‌ها",
                        onClick = onOpenPermissions
                    )
                    Divider()
                    InfoRow(
                        title = "حریم خصوصی",
                        subtitle = "صدا و گفتگوها فقط روی دستگاه شما ذخیره می‌شوند. کلید API به‌صورت رمزنگاری‌شده نگه‌داری می‌شود و هرگز در کد قرار نمی‌گیرد."
                    )
                }
            }

            // ---- About ----
            item {
                SettingsSection("درباره") {
                    InfoRow(
                        title = "بتی هوشمند",
                        subtitle = "دستیار صوتی هوش مصنوعی — هستهٔ پایدار"
                    )
                    Divider()
                    InfoRow(
                        title = "نسخه",
                        subtitle = state.appVersion
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun NavRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

@Composable
private fun LanguageChip(label: String, tag: String, selected: String, onSelect: (String) -> Unit) {
    SelectableChip(label = label, selected = tag == selected, onClick = { onSelect(tag) })
}

@Composable
private fun ProviderChip(label: String, id: String, selected: String, onSelect: (String) -> Unit) {
    SelectableChip(label = label, selected = id == selected, onClick = { onSelect(id) })
}

@Composable
private fun SelectableChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        }
        Text(label, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

@Composable
private fun VoiceSelector(voices: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    Column {
        SelectableChip(label = "پیش‌فرض سیستم", selected = selected == null, onClick = { onSelect(null) })
        Spacer(Modifier.height(8.dp))
        voices.take(8).forEach { voice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(voice) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = voice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (selected == voice) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun ApiKeyField(hasKey: Boolean, onSave: (String) -> Unit, onClear: () -> Unit) {
    var value by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "کلید API",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text(if (hasKey) "کلید ذخیره شده است (برای تغییر وارد کنید)" else "کلید API را وارد کنید") },
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = {
                    if (value.isNotBlank()) {
                        onSave(value.trim())
                        value = ""
                    }
                },
                enabled = value.isNotBlank()
            ) { Text("ذخیرهٔ کلید") }
            if (hasKey) {
                TextButton(onClick = onClear) { Text("حذف کلید") }
            }
        }
    }
}
