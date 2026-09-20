package ru.vahta.calculator.domain

private fun close(a: Double, b: Double, eps: Double = 0.01) = kotlin.math.abs(a - b) <= eps

fun main() {
    val s61 = WorkSchedule(6, 1)
    fun base(type: SalaryType, amount: Double, duration: Int, unit: DurationUnit, hours: Double = 11.0) = VahtaInput(
        salaryType = type,
        amountMax = amount,
        taxStatus = TaxStatus.NET,
        durationValue = duration,
        durationUnit = unit,
        schedule = s61,
        shiftHours = hours,
        interWatchRestDays = 30,
    )

    val tests = mutableListOf<Pair<String, Boolean>>()
    run {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_SHIFT, 4000.0, 60, DurationUnit.CALENDAR_DAYS))
        tests += "60 дней 6/1 -> 52 смены и 208000" to (r.workShifts == 52 && close(r.grossMax, 208000.0))
    }
    run {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_HOUR, 700.0, 60, DurationUnit.WORK_SHIFTS, 12.0))
        tests += "700/час x 12 x 60 = 504000" to close(r.grossMax, 504000.0)
    }
    run {
        val r = VahtaCalculator.calculate(base(SalaryType.WHOLE_WATCH, 360000.0, 60, DurationUnit.CALENDAR_DAYS).copy(travelCost = 23000.0))
        tests += "360000 - 23000 = 337000" to close(r.netAfterExpensesMax, 337000.0)
    }
    run {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_SHIFT, 6500.0, 60, DurationUnit.WORK_SHIFTS))
        tests += "60 смен 6/1 -> 69 календарных дней" to (r.calendarDaysOnWatch == 69 && close(r.grossMax, 390000.0))
    }
    run {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_SHIFT, 0.0, 39, DurationUnit.WORK_SHIFTS).copy(
            nightShiftBreakdown = NightShiftBreakdown(25, 4025.0, 14, 4550.0)
        ))
        tests += "день/ночь = 164325" to close(r.grossMax, 164325.0)
    }
    run {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_SHIFT, 8500.0, 26, DurationUnit.WORK_SHIFTS).copy(
            amountKind = AmountKind.RANGE, amountMin = 4300.0
        ))
        tests += "диапазон 111800-221000" to (close(r.grossMin!!, 111800.0) && close(r.grossMax, 221000.0))
    }
    run {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_SHIFT, 5900.0, 39, DurationUnit.WORK_SHIFTS).copy(amountKind = AmountKind.UP_TO))
        tests += "до 5900 -> максимум 230100 без минимума" to (r.grossMin == null && close(r.grossMax, 230100.0))
    }
    run {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_MONTH, 193570.0, 52, DurationUnit.WORK_SHIFTS).copy(monthlyBasisShifts = 26))
        tests += "193570/26 x 52 = 387140" to close(r.grossMax, 387140.0)
    }
    run {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_HOUR, 700.0, 52, DurationUnit.WORK_SHIFTS, 12.0).copy(
            advertisedHourlyRate = 700.0, advertisedShiftRate = 7500.0
        ))
        tests += "ловим 8400 против 7500" to r.warnings.any { it.contains("8400") && it.contains("7500") }
    }
    run {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_MONTH, 180000.0, 60, DurationUnit.CALENDAR_DAYS))
        tests += "месячная без базы -> ориентировочно" to (r.accuracy == Accuracy.ORIENTING)
    }

    tests.forEachIndexed { index, (name, ok) ->
        println("${index + 1}. ${if (ok) "PASS" else "FAIL"} — $name")
    }
    check(tests.all { it.second }) { "Some engine tests failed" }
    println("\nALL ${tests.size} ENGINE TESTS PASSED")
}
