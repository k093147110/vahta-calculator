package ru.vahta.calculator.domain

import kotlin.math.abs
import kotlin.math.min

object VahtaCalculator {
    private const val AVG_MONTH_DAYS = 30.44

    fun calculate(input: VahtaInput): VahtaResult {
        require(input.amountMax >= 0.0)
        require(input.durationValue > 0)
        require(input.shiftHours > 0.0 && input.shiftHours <= 24.0)
        require(input.interWatchRestDays >= 0)

        val warnings = mutableListOf<String>()

        val standardShifts: Int
        val calendarDays: Int
        if (input.durationUnit == DurationUnit.CALENDAR_DAYS) {
            calendarDays = input.durationValue
            standardShifts = workShiftsInCalendarDays(calendarDays, input.schedule)
        } else {
            standardShifts = input.durationValue
            calendarDays = calendarDaysNeededForShifts(standardShifts, input.schedule)
        }

        val workShifts = input.nightShiftBreakdown?.let {
            it.dayShifts + it.nightShifts
        } ?: standardShifts
        val workHours = workShifts * input.shiftHours

        val baseRange = if (input.nightShiftBreakdown != null) {
            val n = input.nightShiftBreakdown
            val value = n.dayShifts * n.dayRate + n.nightShifts * n.nightRate
            MoneyRange(value, value)
        } else {
            calculateBaseSalary(input, workShifts, calendarDays, warnings)
        }

        val expenses = input.travelCost.coerceAtLeast(0.0) +
            input.foodPerCalendarDay.coerceAtLeast(0.0) * calendarDays +
            input.accommodationCost.coerceAtLeast(0.0) +
            input.otherCosts.coerceAtLeast(0.0)

        val additions = input.bonus.coerceAtLeast(0.0) +
            input.dailyAllowance.coerceAtLeast(0.0) * calendarDays

        val netMin = baseRange.min?.plus(additions)?.minus(expenses)
        val netMax = baseRange.max + additions - expenses

        val hourlyMin = if (workHours > 0) netMin?.div(workHours) else null
        val hourlyMax = if (workHours > 0) netMax / workHours else 0.0

        val cycleDays = calendarDays + input.interWatchRestDays
        val monthlyMin = if (cycleDays > 0) netMin?.div(cycleDays)?.times(AVG_MONTH_DAYS) else null
        val monthlyMax = if (cycleDays > 0) netMax / cycleDays * AVG_MONTH_DAYS else 0.0

        if (input.taxStatus == TaxStatus.UNKNOWN) {
            warnings += "Не указано, зарплата на руки или до налогов."
        }
        checkAdvertisedRates(input, warnings)

        val accuracy = when {
            input.amountKind == AmountKind.UP_TO -> Accuracy.INSUFFICIENT
            warnings.any { it.startsWith("Недостаточно данных") } -> Accuracy.INSUFFICIENT
            warnings.isNotEmpty() || input.amountKind == AmountKind.RANGE -> Accuracy.ORIENTING
            else -> Accuracy.PRECISE
        }

        return VahtaResult(
            workShifts = workShifts,
            calendarDaysOnWatch = calendarDays,
            workHours = workHours,
            grossMin = baseRange.min,
            grossMax = baseRange.max,
            netAfterExpensesMin = netMin,
            netAfterExpensesMax = netMax,
            expenses = expenses,
            additions = additions,
            effectiveHourlyMin = hourlyMin,
            effectiveHourlyMax = hourlyMax,
            monthlyCycleAverageMin = monthlyMin,
            monthlyCycleAverageMax = monthlyMax,
            accuracy = accuracy,
            warnings = warnings.distinct(),
        )
    }

    fun workShiftsInCalendarDays(days: Int, schedule: WorkSchedule): Int {
        require(days > 0)
        val cycle = schedule.workDays + schedule.restDays
        if (schedule.restDays == 0) return days
        val fullCycles = days / cycle
        val remainder = days % cycle
        return fullCycles * schedule.workDays + min(remainder, schedule.workDays)
    }

    fun calendarDaysNeededForShifts(shifts: Int, schedule: WorkSchedule): Int {
        require(shifts > 0)
        if (schedule.restDays == 0) return shifts
        val completedRestBlocksBeforeLastShift = (shifts - 1) / schedule.workDays
        return shifts + completedRestBlocksBeforeLastShift * schedule.restDays
    }

    private fun calculateBaseSalary(
        input: VahtaInput,
        workShifts: Int,
        calendarDays: Int,
        warnings: MutableList<String>,
    ): MoneyRange {
        val raw = when (input.amountKind) {
            AmountKind.EXACT -> MoneyRange(input.amountMax, input.amountMax)
            AmountKind.RANGE -> MoneyRange(input.amountMin ?: 0.0, input.amountMax)
            AmountKind.UP_TO -> {
                warnings += "Недостаточно данных: работодатель указал только максимальную сумму «до»."
                MoneyRange(null, input.amountMax)
            }
        }

        fun transform(value: Double): Double = when (input.salaryType) {
            SalaryType.PER_HOUR -> value * input.shiftHours * workShifts
            SalaryType.PER_SHIFT -> value * workShifts
            SalaryType.WHOLE_WATCH -> value
            SalaryType.PER_MONTH -> {
                val basis = input.monthlyBasisShifts
                if (basis != null && basis > 0) {
                    value / basis * workShifts
                } else {
                    if (warnings.none { it.contains("месячная зарплата") }) {
                        warnings += "Месячная зарплата не привязана к числу смен — расчёт по календарным дням ориентировочный."
                    }
                    value * calendarDays / AVG_MONTH_DAYS
                }
            }
        }

        return MoneyRange(raw.min?.let(::transform), transform(raw.max))
    }

    private fun checkAdvertisedRates(input: VahtaInput, warnings: MutableList<String>) {
        val hour = input.advertisedHourlyRate
        val shift = input.advertisedShiftRate
        if (hour != null && shift != null && input.shiftHours > 0) {
            val expected = hour * input.shiftHours
            if (abs(expected - shift) >= 1.0) {
                val delta = expected - shift
                warnings += "Цифры работодателя не совпадают: по почасовой ставке смена должна стоить %.0f ₽, а указано %.0f ₽ (разница %.0f ₽ за смену)."
                    .format(expected, shift, delta)
            }
        }
    }

    private data class MoneyRange(val min: Double?, val max: Double)
}
