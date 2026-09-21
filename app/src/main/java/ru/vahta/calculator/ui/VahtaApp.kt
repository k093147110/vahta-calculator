package ru.vahta.calculator.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.vahta.calculator.domain.AmountKind
import ru.vahta.calculator.domain.DurationUnit
import ru.vahta.calculator.domain.SalaryType
import ru.vahta.calculator.domain.TaxStatus
import ru.vahta.calculator.domain.VahtaCalculator
import ru.vahta.calculator.domain.VahtaInput
import ru.vahta.calculator.domain.VahtaResult
import ru.vahta.calculator.domain.WorkSchedule
import ru.vahta.calculator.ui.theme.AppBorder
import ru.vahta.calculator.ui.theme.AppOrange
import ru.vahta.calculator.ui.theme.AppSurface
import ru.vahta.calculator.ui.theme.AppSurfaceAlt
import ru.vahta.calculator.ui.theme.InputSurface
import ru.vahta.calculator.ui.theme.MoneyGreen
import ru.vahta.calculator.ui.theme.MutedText
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private const val SCREEN_INPUT = "input"
private const val SCREEN_RESULT = "result"
private const val SCREEN_COMPARE = "compare"
private const val SCREEN_CHECKS = "checks"
private const val SCREEN_SETTINGS = "settings"

private enum class PurchaseProduct {
    REMOVE_ADS,
    SAVE_TO_CHECKS,
}

private data class VacancyDraft(
    val dailyRate: String = "3500",
    val durationDays: String = "30",
    val workSchedule: String = "6/1",
    val shiftHours: String = "11",
    val travel: String = "5000",
    val foodPerDay: String = "400",
    val workwear: String = "3000",
    val fines: String = "0",
)

private data class NamedResult(
    val vacancyName: String,
    val draft: VacancyDraft,
    val result: VahtaResult,
)

private data class SavedCheck(
    val title: String,
    val date: String,
    val durationDays: Int,
    val netIncome: Double,
    val effectiveHourly: Double,
    val expenses: Double,
)

private enum class MainTab(
    val label: String,
    val icon: ImageVector,
) {
    CALCULATOR("Калькулятор", Icons.Outlined.Calculate),
    COMPARE("Сравнение", Icons.Outlined.CompareArrows),
    CHECKS("Чеки", Icons.Outlined.ReceiptLong),
    SETTINGS("Настройки", Icons.Outlined.Settings),
}

@Composable
fun VahtaApp() {
    var screen by rememberSaveable { mutableStateOf(SCREEN_INPUT) }
    var selectedVacancy by rememberSaveable { mutableStateOf(1) }
    var vacancy1 by remember { mutableStateOf(VacancyDraft()) }
    var vacancy2 by remember {
        mutableStateOf(
            VacancyDraft(
                dailyRate = "4000",
                durationDays = "30",
                workSchedule = "5/2",
                shiftHours = "12",
                travel = "3000",
                foodPerDay = "300",
                workwear = "4000",
            )
        )
    }
    var singleResult by remember { mutableStateOf<NamedResult?>(null) }
    var comparisonResults by remember { mutableStateOf<Pair<VahtaResult, VahtaResult>?>(null) }
    var adsRemoved by rememberSaveable { mutableStateOf(false) }
    var saveUnlocked by rememberSaveable { mutableStateOf(false) }
    var dialogMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val savedChecks = remember { mutableStateListOf<SavedCheck>() }

    fun requestPurchase(product: PurchaseProduct) {
        dialogMessage = when (product) {
            PurchaseProduct.REMOVE_ADS ->
                "Отключение рекламы — отдельная покупка за 99 ₽. Интерфейс готов, но для реального списания нужно подключить биллинг магазина приложений."

            PurchaseProduct.SAVE_TO_CHECKS ->
                "Сохранение расчётов — отдельная покупка за 99 ₽. Интерфейс готов, но для реального списания нужно подключить биллинг магазина приложений."
        }
    }

    fun openComparison() {
        try {
            comparisonResults = calculateDraft(vacancy1) to calculateDraft(vacancy2)
            screen = SCREEN_COMPARE
        } catch (e: Exception) {
            dialogMessage = e.message?.takeIf { it.isNotBlank() }
                ?: "Проверьте числа в обеих вакансиях перед сравнением."
        }
    }

    val activeTab = when (screen) {
        SCREEN_COMPARE -> MainTab.COMPARE
        SCREEN_CHECKS -> MainTab.CHECKS
        SCREEN_SETTINGS -> MainTab.SETTINGS
        else -> MainTab.CALCULATOR
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(
                selected = activeTab,
                onSelected = { tab ->
                    when (tab) {
                        MainTab.CALCULATOR -> screen = SCREEN_INPUT
                        MainTab.COMPARE -> openComparison()
                        MainTab.CHECKS -> screen = SCREEN_CHECKS
                        MainTab.SETTINGS -> screen = SCREEN_SETTINGS
                    }
                },
            )
        },
    ) { padding ->
        when (screen) {
            SCREEN_RESULT -> {
                val named = singleResult
                if (named == null) {
                    screen = SCREEN_INPUT
                } else {
                    ResultScreen(
                        modifier = Modifier.padding(padding),
                        namedResult = named,
                        selectedVacancy = selectedVacancy,
                        onSelectedVacancyChange = { target ->
                            selectedVacancy = target
                            try {
                                val draft = if (target == 1) vacancy1 else vacancy2
                                singleResult = NamedResult(
                                    vacancyName = "Вакансия $target",
                                    draft = draft,
                                    result = calculateDraft(draft),
                                )
                            } catch (_: Exception) {
                                screen = SCREEN_INPUT
                            }
                        },
                        saveUnlocked = saveUnlocked,
                        onSave = {
                            if (saveUnlocked) {
                                savedChecks += named.toSavedCheck()
                                dialogMessage = "Расчёт сохранён в разделе «Чеки»."
                            } else {
                                requestPurchase(PurchaseProduct.SAVE_TO_CHECKS)
                            }
                        },
                        onEdit = { screen = SCREEN_INPUT },
                    )
                }
            }

            SCREEN_COMPARE -> {
                val pair = comparisonResults
                if (pair == null) {
                    openComparison()
                } else {
                    ComparisonScreen(
                        modifier = Modifier.padding(padding),
                        vacancy1Draft = vacancy1,
                        vacancy1 = pair.first,
                        vacancy2Draft = vacancy2,
                        vacancy2 = pair.second,
                        selectedVacancy = selectedVacancy,
                        onSelectedVacancyChange = { selectedVacancy = it },
                        onEdit = { screen = SCREEN_INPUT },
                    )
                }
            }

            SCREEN_CHECKS -> ChecksScreen(
                modifier = Modifier.padding(padding),
                savedChecks = savedChecks,
                saveUnlocked = saveUnlocked,
                onPurchaseSave = { requestPurchase(PurchaseProduct.SAVE_TO_CHECKS) },
            )

            SCREEN_SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(padding),
                adsRemoved = adsRemoved,
                saveUnlocked = saveUnlocked,
                onPurchaseRemoveAds = { requestPurchase(PurchaseProduct.REMOVE_ADS) },
                onPurchaseSave = { requestPurchase(PurchaseProduct.SAVE_TO_CHECKS) },
                onRestorePurchases = {
                    dialogMessage = "Восстановление покупок будет работать после подключения биллинга магазина приложений."
                },
            )

            else -> CalculatorScreen(
                modifier = Modifier.padding(padding),
                selectedVacancy = selectedVacancy,
                onSelectedVacancyChange = { selectedVacancy = it },
                draft = if (selectedVacancy == 1) vacancy1 else vacancy2,
                onDraftChange = { updated ->
                    if (selectedVacancy == 1) vacancy1 = updated else vacancy2 = updated
                },
                adsRemoved = adsRemoved,
                saveUnlocked = saveUnlocked,
                onPurchaseRemoveAds = { requestPurchase(PurchaseProduct.REMOVE_ADS) },
                onPurchaseSave = { requestPurchase(PurchaseProduct.SAVE_TO_CHECKS) },
                onCalculate = {
                    try {
                        val draft = if (selectedVacancy == 1) vacancy1 else vacancy2
                        singleResult = NamedResult(
                            vacancyName = "Вакансия $selectedVacancy",
                            draft = draft,
                            result = calculateDraft(draft),
                        )
                        screen = SCREEN_RESULT
                        null
                    } catch (e: Exception) {
                        e.message?.takeIf { it.isNotBlank() } ?: "Проверьте числа в заполненных полях."
                    }
                },
            )
        }
    }

    dialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { dialogMessage = null },
            title = { Text("Вахта Калькулятор") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { dialogMessage = null }) {
                    Text("Понятно")
                }
            },
        )
    }

    @Suppress("UNUSED_VARIABLE")
    val purchaseEntitlements = adsRemoved to saveUnlocked
}

@Composable
private fun CalculatorScreen(
    modifier: Modifier = Modifier,
    selectedVacancy: Int,
    onSelectedVacancyChange: (Int) -> Unit,
    draft: VacancyDraft,
    onDraftChange: (VacancyDraft) -> Unit,
    adsRemoved: Boolean,
    saveUnlocked: Boolean,
    onPurchaseRemoveAds: () -> Unit,
    onPurchaseSave: () -> Unit,
    onCalculate: () -> String?,
) {
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    ScreenColumn(modifier) {
        AppHeader()
        VacancySwitch(
            selectedVacancy = selectedVacancy,
            onSelectedVacancyChange = {
                error = null
                onSelectedVacancyChange(it)
            },
        )

        SectionTitle("Основные параметры")
        GroupCard {
            InputRow("Ставка в день, ₽", draft.dailyRate) {
                onDraftChange(draft.copy(dailyRate = it))
            }
            CardDivider()
            InputRow("Длительность вахты, дней", draft.durationDays) {
                onDraftChange(draft.copy(durationDays = it))
            }
            CardDivider()
            ScheduleInputRow("График работы", draft.workSchedule) {
                onDraftChange(draft.copy(workSchedule = it))
            }
            CardDivider()
            InputRow("Часов в день", draft.shiftHours) {
                onDraftChange(draft.copy(shiftHours = it))
            }
        }

        Row(verticalAlignment = Alignment.Bottom) {
            SectionTitle("Расходы")
            Text(
                "  (указывайте до расчёта)",
                color = MutedText,
                fontSize = 13.sp,
            )
        }
        GroupCard {
            InputRow("Дорога за свой счёт, ₽", draft.travel) {
                onDraftChange(draft.copy(travel = it))
            }
            CardDivider()
            InputRow("Питание в день, ₽", draft.foodPerDay) {
                onDraftChange(draft.copy(foodPerDay = it))
            }
            CardDivider()
            InputRow("Спецодежда / форма, ₽", draft.workwear) {
                onDraftChange(draft.copy(workwear = it))
            }
            CardDivider()
            InputRow("Штрафы (если есть), ₽", draft.fines) {
                onDraftChange(draft.copy(fines = it))
            }
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        OrangeButton(
            text = "Рассчитать",
            onClick = { error = onCalculate() },
        )

        if (!adsRemoved) {
            YandexAdPlaceholder()
            PurchaseCard(
                icon = Icons.Outlined.Star,
                title = "Отключить рекламу — 99 ₽",
                subtitle = "Без рекламы навсегда",
                onClick = onPurchaseRemoveAds,
            )
        }

        if (!saveUnlocked) {
            PurchaseCard(
                icon = Icons.Outlined.Save,
                title = "Сохранить расчет в чеки — 99 ₽",
                subtitle = "Сохраняйте свои расчёты",
                onClick = onPurchaseSave,
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ResultScreen(
    modifier: Modifier = Modifier,
    namedResult: NamedResult,
    selectedVacancy: Int,
    onSelectedVacancyChange: (Int) -> Unit,
    saveUnlocked: Boolean,
    onSave: () -> Unit,
    onEdit: () -> Unit,
) {
    val result = namedResult.result
    val draft = namedResult.draft
    val duration = draft.durationDays.toIntOrNull()?.coerceAtLeast(1) ?: result.calendarDaysOnWatch.coerceAtLeast(1)
    val netPerDay = result.netAfterExpensesMax / duration

    ScreenColumn(modifier) {
        AppHeader()
        VacancySwitch(selectedVacancy, onSelectedVacancyChange)
        SectionTitle("Результат")

        GroupCard {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Чистый доход за вахту", fontSize = 15.sp)
                MoneyText(formatRubles(result.netAfterExpensesMax), 34.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Чистый доход\nв день",
                value = formatRubles(netPerDay),
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Реальный доход\nв час",
                value = formatRubles(result.effectiveHourlyMax),
            )
        }

        SectionTitle("Доходы")
        GroupCard {
            SummaryRow(
                label = "Заработок (${formatRubles(draft.dailyRate.toMoneyDouble())} × ${result.workShifts} смен)",
                value = formatRubles(result.grossMax),
                money = true,
            )
            CardDivider()
            SummaryRow(
                label = "График / часов в день",
                value = "${draft.workSchedule} / ${draft.shiftHours} ч",
            )
        }

        SectionTitle("Расходы")
        GroupCard {
            SummaryRow("Дорога за свой счёт", formatRubles(draft.travel.toMoneyDouble()))
            CardDivider()
            SummaryRow(
                "Питание (${formatRubles(draft.foodPerDay.toMoneyDouble())} × $duration дн.)",
                formatRubles(draft.foodPerDay.toMoneyDouble() * duration),
            )
            CardDivider()
            SummaryRow("Спецодежда / форма", formatRubles(draft.workwear.toMoneyDouble()))
            CardDivider()
            SummaryRow("Штрафы", formatRubles(draft.fines.toMoneyDouble()))
            CardDivider()
            SummaryRow("Итого расходы", formatRubles(result.expenses), emphasize = true)
        }

        GroupCard {
            SummaryRow(
                label = "Чистый доход за вахту",
                value = formatRubles(result.netAfterExpensesMax),
                money = true,
                emphasize = true,
            )
        }

        PurchaseAwareSaveButton(
            saveUnlocked = saveUnlocked,
            onClick = onSave,
        )
        OrangeButton("Изменить данные", onEdit)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ComparisonScreen(
    modifier: Modifier = Modifier,
    vacancy1Draft: VacancyDraft,
    vacancy1: VahtaResult,
    vacancy2Draft: VacancyDraft,
    vacancy2: VahtaResult,
    selectedVacancy: Int,
    onSelectedVacancyChange: (Int) -> Unit,
    onEdit: () -> Unit,
) {
    val net1 = vacancy1.netAfterExpensesMax
    val net2 = vacancy2.netAfterExpensesMax
    val hour1 = vacancy1.effectiveHourlyMax
    val hour2 = vacancy2.effectiveHourlyMax
    val winner = when {
        abs(net1 - net2) < 0.01 -> 0
        net1 > net2 -> 1
        else -> 2
    }

    ScreenColumn(modifier) {
        AppHeader()
        VacancySwitch(selectedVacancy, onSelectedVacancyChange)
        SectionTitle("Сравнение вакансий")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ComparisonHero(
                modifier = Modifier.weight(1f),
                title = "Вакансия 1",
                netIncome = net1,
                perDay = net1 / vacancy1.calendarDaysOnWatch.coerceAtLeast(1),
                perHour = hour1,
            )
            ComparisonHero(
                modifier = Modifier.weight(1f),
                title = "Вакансия 2",
                netIncome = net2,
                perDay = net2 / vacancy2.calendarDaysOnWatch.coerceAtLeast(1),
                perHour = hour2,
            )
        }

        WinnerCard(
            winner = winner,
            netDifference = abs(net1 - net2),
            hourlyDifference = abs(hour1 - hour2),
        )

        SectionTitle("Краткое сравнение")
        GroupCard {
            ComparisonTableRow(
                label = "Ставка в день",
                left = formatRubles(vacancy1Draft.dailyRate.toMoneyDouble()),
                right = formatRubles(vacancy2Draft.dailyRate.toMoneyDouble()),
                highlightLeft = vacancy1Draft.dailyRate.toMoneyDouble() > vacancy2Draft.dailyRate.toMoneyDouble(),
                highlightRight = vacancy2Draft.dailyRate.toMoneyDouble() > vacancy1Draft.dailyRate.toMoneyDouble(),
            )
            CardDivider()
            ComparisonTableRow(
                label = "Дней вахты",
                left = vacancy1Draft.durationDays,
                right = vacancy2Draft.durationDays,
            )
            CardDivider()
            ComparisonTableRow(
                label = "График работы",
                left = vacancy1Draft.workSchedule,
                right = vacancy2Draft.workSchedule,
            )
            CardDivider()
            ComparisonTableRow(
                label = "Часов в день",
                left = vacancy1Draft.shiftHours,
                right = vacancy2Draft.shiftHours,
            )
            CardDivider()
            ComparisonTableRow(
                label = "Расходы всего",
                left = formatRubles(vacancy1.expenses),
                right = formatRubles(vacancy2.expenses),
                highlightLeft = vacancy1.expenses < vacancy2.expenses,
                highlightRight = vacancy2.expenses < vacancy1.expenses,
            )
            CardDivider()
            ComparisonTableRow(
                label = "Чистый доход",
                left = formatRubles(net1),
                right = formatRubles(net2),
                highlightLeft = net1 > net2,
                highlightRight = net2 > net1,
            )
            CardDivider()
            ComparisonTableRow(
                label = "Доход в час",
                left = formatRubles(hour1),
                right = formatRubles(hour2),
                highlightLeft = hour1 > hour2,
                highlightRight = hour2 > hour1,
            )
        }

        OrangeButton("Перейти к редактированию", onEdit)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ChecksScreen(
    modifier: Modifier = Modifier,
    savedChecks: List<SavedCheck>,
    saveUnlocked: Boolean,
    onPurchaseSave: () -> Unit,
) {
    ScreenColumn(modifier) {
        Text("Мои расчеты (чеки)", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        if (savedChecks.isEmpty()) {
            GroupCard {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Пока нет сохранённых расчётов.", fontWeight = FontWeight.SemiBold)
                    Text(
                        if (saveUnlocked) {
                            "Откройте результат расчёта и нажмите «Сохранить в чеки»."
                        } else {
                            "Сохранение расчётов доступно отдельной покупкой."
                        },
                        color = MutedText,
                        fontSize = 13.sp,
                    )
                }
            }
            if (!saveUnlocked) {
                PurchaseCard(
                    icon = Icons.Outlined.Save,
                    title = "Сохранить расчет в чеки — 99 ₽",
                    subtitle = "Сохраняйте свои расчёты",
                    onClick = onPurchaseSave,
                )
            }
        } else {
            savedChecks.asReversed().forEach { check ->
                CheckCard(check)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    adsRemoved: Boolean,
    saveUnlocked: Boolean,
    onPurchaseRemoveAds: () -> Unit,
    onPurchaseSave: () -> Unit,
    onRestorePurchases: () -> Unit,
) {
    ScreenColumn(modifier) {
        Text("Настройки", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        SettingsCard(
            icon = Icons.Outlined.Star,
            title = "Отключить рекламу",
            value = if (adsRemoved) "Куплено" else "99 ₽",
            valueGreen = adsRemoved,
            onClick = onPurchaseRemoveAds,
        )
        SettingsCard(
            icon = Icons.Outlined.Save,
            title = "Сохранить расчет в чеки",
            value = if (saveUnlocked) "Куплено" else "99 ₽",
            valueGreen = saveUnlocked,
            onClick = onPurchaseSave,
        )
        SettingsCard(
            icon = Icons.Outlined.Refresh,
            title = "Восстановить покупки",
            value = "›",
            onClick = onRestorePurchases,
        )
        SettingsCard(
            icon = Icons.Outlined.Info,
            title = "О приложении",
            value = "Версия 0.5.0",
            onClick = null,
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AppHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Вахта Калькулятор", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            "Посчитай, сколько ты реально заработаешь",
            color = MutedText,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun VacancySwitch(
    selectedVacancy: Int,
    onSelectedVacancyChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppSurface, RoundedCornerShape(10.dp))
            .border(1.dp, AppBorder, RoundedCornerShape(10.dp)),
    ) {
        SegmentButton(
            modifier = Modifier.weight(1f),
            text = "Вакансия 1",
            selected = selectedVacancy == 1,
            onClick = { onSelectedVacancyChange(1) },
        )
        SegmentButton(
            modifier = Modifier.weight(1f),
            text = "Вакансия 2",
            selected = selectedVacancy == 2,
            onClick = { onSelectedVacancyChange(2) },
        )
    }
}

@Composable
private fun SegmentButton(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                color = if (selected) AppOrange else AppSurface,
                shape = RoundedCornerShape(9.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MutedText,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun GroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            content = content,
        )
    }
}

@Composable
private fun ScheduleInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
        )
        BasicTextField(
            value = value,
            onValueChange = { next ->
                if (next.length <= 16 && !next.contains('\n')) onValueChange(next)
            },
            modifier = Modifier.width(108.dp),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                textAlign = TextAlign.End,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            cursorBrush = SolidColor(AppOrange),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InputSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, AppBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun InputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
        )
        BasicTextField(
            value = value,
            onValueChange = { next ->
                if (next.all { it.isDigit() || it == '.' || it == ',' }) onValueChange(next)
            },
            modifier = Modifier.width(108.dp),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                textAlign = TextAlign.End,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            cursorBrush = SolidColor(AppOrange),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InputSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, AppBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    money: Boolean = false,
    emphasize: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            value,
            color = if (money) MoneyGreen else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (money || emphasize) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (emphasize) 16.sp else 14.sp,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier,
    label: String,
    value: String,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(label, textAlign = TextAlign.Center, fontSize = 13.sp)
            MoneyText(value, 22.sp)
        }
    }
}

@Composable
private fun ComparisonHero(
    modifier: Modifier,
    title: String,
    netIncome: Double,
    perDay: Double,
    perHour: Double,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            MoneyText(formatRubles(netIncome), 22.sp)
            Text("чистый доход\nза вахту", textAlign = TextAlign.Center, fontSize = 12.sp)
            CardDivider()
            MoneyText(formatRubles(perDay), 18.sp)
            Text("в день", color = MutedText, fontSize = 12.sp)
            CardDivider()
            MoneyText(formatRubles(perHour), 18.sp)
            Text("в час", color = MutedText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun WinnerCard(
    winner: Int,
    netDifference: Double,
    hourlyDifference: Double,
) {
    val title = if (winner == 0) "Доход одинаковый" else "🏆 Вакансия $winner выгоднее!"
    val line1 = if (winner == 0) {
        "Чистый доход по обеим вакансиям одинаковый."
    } else {
        "Чистый доход больше на ${formatRubles(netDifference)}"
    }
    val line2 = when {
        hourlyDifference < 0.01 -> "Реальный доход в час одинаковый"
        else -> "Разница реального дохода в час — ${formatRubles(hourlyDifference)}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, MoneyGreen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, color = MoneyGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(line1, fontSize = 13.sp)
            Text(line2, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ComparisonTableRow(
    label: String,
    left: String,
    right: String,
    highlightLeft: Boolean = false,
    highlightRight: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1.3f), fontSize = 12.sp)
        Text(
            left,
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.End,
            fontSize = 12.sp,
            color = if (highlightLeft) MoneyGreen else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (highlightLeft) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            right,
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.End,
            fontSize = 12.sp,
            color = if (highlightRight) MoneyGreen else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (highlightRight) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun CheckCard(check: SavedCheck) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(check.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(check.date, color = MutedText, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${check.durationDays} дней • ", color = MutedText, fontSize = 13.sp)
                Text(formatRubles(check.netIncome), color = MoneyGreen, fontWeight = FontWeight.Bold)
            }
            Text(
                "Реальный час: ${formatRubles(check.effectiveHourly)} · Расходы: ${formatRubles(check.expenses)}",
                color = MutedText,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    value: String,
    valueGreen: Boolean = false,
    onClick: (() -> Unit)?,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { base -> if (onClick != null) base.clickable(onClick = onClick) else base },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = AppOrange, modifier = Modifier.size(23.dp))
            Text(title, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text(
                value,
                color = if (valueGreen) MoneyGreen else MutedText,
                fontWeight = if (valueGreen) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun PurchaseCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = AppOrange, modifier = Modifier.size(27.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, color = MutedText, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun YandexAdPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = CardDefaults.cardColors(containerColor = AppSurfaceAlt),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            AppSurfaceAlt,
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Яндекс Реклама", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Здесь будет ваш рекламный баннер", color = MutedText, fontSize = 12.sp)
            }
            Text(
                "Реклама",
                modifier = Modifier.align(Alignment.TopEnd),
                color = MutedText,
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun PurchaseAwareSaveButton(
    saveUnlocked: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppSurface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(1.dp, AppBorder),
    ) {
        Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            if (saveUnlocked) "Сохранить в чеки" else "Сохранить расчет в чеки — 99 ₽",
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun OrangeButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppOrange,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun BottomNavigationBar(
    selected: MainTab,
    onSelected: (MainTab) -> Unit,
) {
    NavigationBar(
        containerColor = AppSurface,
        tonalElevation = 0.dp,
    ) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelected(tab) },
                icon = {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(22.dp),
                    )
                },
                label = {
                    Text(tab.label, fontSize = 10.sp, maxLines = 1)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppOrange,
                    selectedTextColor = AppOrange,
                    indicatorColor = AppSurfaceAlt,
                    unselectedIconColor = MutedText,
                    unselectedTextColor = MutedText,
                ),
            )
        }
    }
}

@Composable
private fun ScreenColumn(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
}

@Composable
private fun CardDivider() {
    HorizontalDivider(color = AppBorder, thickness = 1.dp)
}

@Composable
private fun MoneyText(text: String, size: androidx.compose.ui.unit.TextUnit) {
    Text(
        text,
        color = MoneyGreen,
        fontWeight = FontWeight.Bold,
        fontSize = size,
    )
}

private fun calculateDraft(draft: VacancyDraft): VahtaResult {
    val input = VahtaInput(
        salaryType = SalaryType.PER_SHIFT,
        amountKind = AmountKind.EXACT,
        amountMax = draft.dailyRate.toRequiredDouble(),
        taxStatus = TaxStatus.UNKNOWN,
        durationValue = draft.durationDays.toInt(),
        durationUnit = DurationUnit.CALENDAR_DAYS,
        schedule = draft.workSchedule.toWorkSchedule(),
        shiftHours = draft.shiftHours.toRequiredDouble(),
        interWatchRestDays = 0,
        travelCost = draft.travel.toMoneyDouble(),
        foodPerCalendarDay = draft.foodPerDay.toMoneyDouble(),
        otherCosts = draft.workwear.toMoneyDouble() + draft.fines.toMoneyDouble(),
    )
    return VahtaCalculator.calculate(input)
}

private fun NamedResult.toSavedCheck(): SavedCheck {
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU"))
    return SavedCheck(
        title = vacancyName,
        date = formatter.format(Date()),
        durationDays = draft.durationDays.toIntOrNull() ?: result.calendarDaysOnWatch,
        netIncome = result.netAfterExpensesMax,
        effectiveHourly = result.effectiveHourlyMax,
        expenses = result.expenses,
    )
}

private fun formatRubles(value: Double): String = "${formatNumber(value)} ₽"

private fun formatNumber(value: Double): String =
    NumberFormat.getNumberInstance(Locale("ru", "RU")).format(value)

private fun String.toRequiredDouble(): Double = replace(',', '.').toDouble()

private fun String.toWorkSchedule(): WorkSchedule {
    val parts = Regex("\\d+").findAll(trim()).map { it.value.toInt() }.toList()
    require(parts.size == 2) { "Укажите график работы в формате 6/1, 5/2 или 7/7." }
    require(parts[0] > 0 && parts[1] >= 0) { "В графике число рабочих дней должно быть больше нуля." }
    return WorkSchedule(workDays = parts[0], restDays = parts[1])
}

private fun String.toMoneyDouble(): Double = replace(',', '.').toDoubleOrNull() ?: 0.0
