package com.struperto.androidappdays.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePlanRepository(
    plans: List<PlanItem> = emptyList(),
) : PlanRepository {
    private val plansFlow = MutableStateFlow(plans)

    override fun observeToday(date: String): Flow<List<PlanItem>> = plansFlow

    override fun observeRange(
        startDate: String,
        endDate: String,
    ): Flow<List<PlanItem>> = plansFlow

    override fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int> {
        return MutableStateFlow(plansFlow.value.size)
    }

    override suspend fun addFromVorhaben(
        vorhabenId: String,
        timeBlock: TimeBlock,
    ) = Unit

    override suspend fun addManual(
        title: String,
        note: String,
        areaId: String,
        timeBlock: TimeBlock,
    ) = Unit

    override suspend fun loadById(id: String): PlanItem? {
        return plansFlow.value.firstOrNull { it.id == id }
    }

    override suspend fun toggleDone(id: String) = Unit

    override suspend fun moveToTimeBlock(
        id: String,
        timeBlock: TimeBlock,
    ) = Unit

    override suspend fun removeFromToday(id: String) = Unit
}
