package com.nacare.capture.ui.v2.filters

interface FilterBottomSheetListener {
    fun onStatusClicked(status: String)
    fun onDateClick(date: String)
    fun onDateRangeClicked()

}