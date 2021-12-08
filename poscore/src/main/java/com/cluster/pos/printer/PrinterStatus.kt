package com.cluster.pos.printer

enum class PrinterStatus(val message: String) {
    READY("Printer Ready"),
    NO_PAPER("No paper"),
    OVER_HEAT("Printer Overheating"),
    NOT_READY("Printer not ready"),
    LOW_BATTERY("Battery Low"),
    IMAGE_NOT_FOUND("Image not found"),
    NO_BLACK_BLOCK("Cannot find black block")
}