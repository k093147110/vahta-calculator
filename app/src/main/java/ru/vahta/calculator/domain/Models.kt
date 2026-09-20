package ru.vahta.calculator.domain

enum class SalaryType { PER_HOUR, PER_SHIFT, PER_MONTH, WHOLE_WATCH }
enum class AmountKind { EXACT, RANGE, UP_TO }
enum class TaxStatus { NET, GROSS, UNKNOWN }
enum class DurationUnit { CALENDAR_DAYS, WORK_SHIFTS }
enum class Accuracy { PRECISE, ORIENTING, INSUFFICIENT }

data class WorkSchedule(
    val workDays: Int,
    val restDays: Int,
) {
    init {
        require(workDays > 0) { "workDays must be > 0" }
        require(restDays >= 0) { "restDays must be >= 0" }
    }
}

data class NightShiftBreakdown(
    val dayShifts: Int,
    val dayRate: Double,
    val nightShifts: Int,
    val nightRate: Double,
)

data class VahtaInput(
    val salaryType: SalaryType,
    val amountKind: AmountKind = AmountKind.EXACT,
    val amountMin: Double? = null,
    val amountMax: Double,
    val taxStatus: TaxStatus = TaxStatus.UNKNOWN,
    val monthlyBasisShifts: Int? = null,
    val durationValue: Int,
    val durationUnit: DurationUnit,
    val schedule: WorkSchedule,
    val shiftHours: Double,
    val interWatchRestDays: Int,
    val travelCost: Double = 0.0,
    val foodPerCalendarDay: Double = 0.0,
    val accommodationCost: Double = 0.0,
    val otherCosts: Double = 0.0,
    val bonus: Double = 0.0,
    val dailyAllowance: Double = 0.0,
    val nightShiftBreakdown: NightShiftBreakdown? = null,
    val advertisedHourlyRate: Double? = null,
    val advertisedShiftRate: Double? = null,
)

data class VahtaResult(
    val workShifts: Int,
    val calendarDaysOnWatch: Int,
    val workHours: Double,
    val grossMin: Double?,
    val grossMax: Double,
    val netAfterExpensesMin: Double?,
    val netAfterExpensesMax: Double,
    val expenses: Double,
    val additions: Double,
    val effectiveHourlyMin: Double?,
    val effectiveHourlyMax: Double,
    val monthlyCycleAverageMin: Double?,
    val monthlyCycleAverageMax: Double,
    val accuracy: Accuracy,
    val warnings: List<String>,
)
