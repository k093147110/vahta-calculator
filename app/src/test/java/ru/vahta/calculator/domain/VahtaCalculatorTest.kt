package ru.vahta.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VahtaCalculatorTest {
    private val schedule61 = WorkSchedule(6, 1)

    @Test fun `60 calendar days 6-1 gives 52 shifts`() {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_SHIFT, 4000.0, 60, DurationUnit.CALENDAR_DAYS))
        assertEquals(52, r.workShifts)
        assertEquals(208000.0, r.grossMax, 0.01)
    }

    @Test fun `hourly 700 x 12 x 60 shifts`() {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_HOUR, 700.0, 60, DurationUnit.WORK_SHIFTS, shiftHours = 12.0))
        assertEquals(504000.0, r.grossMax, 0.01)
    }

    @Test fun `whole watch minus expenses`() {
        val r = VahtaCalculator.calculate(base(SalaryType.WHOLE_WATCH, 360000.0, 60, DurationUnit.CALENDAR_DAYS).copy(travelCost = 23000.0))
        assertEquals(337000.0, r.netAfterExpensesMax, 0.01)
    }

    @Test fun `60 work shifts at 6-1 take 69 calendar days`() {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_SHIFT, 6500.0, 60, DurationUnit.WORK_SHIFTS))
        assertEquals(390000.0, r.grossMax, 0.01)
        assertEquals(69, r.calendarDaysOnWatch)
    }

    @Test fun `day and night shift breakdown`() {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_SHIFT, 0.0, 39, DurationUnit.WORK_SHIFTS).copy(
            nightShiftBreakdown = NightShiftBreakdown(25, 4025.0, 14, 4550.0)
        ))
        assertEquals(164325.0, r.grossMax, 0.01)
    }

    @Test fun `range 4300 to 8500 times 26`() {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_SHIFT, 8500.0, 26, DurationUnit.WORK_SHIFTS).copy(
            amountKind = AmountKind.RANGE, amountMin = 4300.0
        ))
        assertEquals(111800.0, r.grossMin!!, 0.01)
        assertEquals(221000.0, r.grossMax, 0.01)
    }

    @Test fun `up to 5900 times 39 has no guaranteed min`() {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_SHIFT, 5900.0, 39, DurationUnit.WORK_SHIFTS).copy(amountKind = AmountKind.UP_TO))
        assertEquals(null, r.grossMin)
        assertEquals(230100.0, r.grossMax, 0.01)
        assertEquals(Accuracy.INSUFFICIENT, r.accuracy)
    }

    @Test fun `monthly basis 193570 per 26 shifts scales to 52`() {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_MONTH, 193570.0, 52, DurationUnit.WORK_SHIFTS).copy(monthlyBasisShifts = 26))
        assertEquals(387140.0, r.grossMax, 0.01)
    }

    @Test fun `advertised hourly and shift mismatch creates warning`() {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_HOUR, 700.0, 52, DurationUnit.WORK_SHIFTS, shiftHours = 12.0).copy(
            advertisedHourlyRate = 700.0,
            advertisedShiftRate = 7500.0,
        ))
        assertTrue(r.warnings.any { it.contains("8400") && it.contains("7500") })
    }

    @Test fun `monthly without shift basis is orienting`() {
        val r = VahtaCalculator.calculate(base(SalaryType.PER_MONTH, 180000.0, 60, DurationUnit.CALENDAR_DAYS))
        assertEquals(Accuracy.ORIENTING, r.accuracy)
        assertTrue(r.warnings.any { it.contains("ориентировочный") })
    }

    private fun base(
        salaryType: SalaryType,
        amount: Double,
        duration: Int,
        unit: DurationUnit,
        shiftHours: Double = 11.0,
    ) = VahtaInput(
        salaryType = salaryType,
        amountMax = amount,
        taxStatus = TaxStatus.NET,
        durationValue = duration,
        durationUnit = unit,
        schedule = schedule61,
        shiftHours = shiftHours,
        interWatchRestDays = 30,
    )
}
