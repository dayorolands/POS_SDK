rootProject.name = "Cluster"

include(":app")
include(":core")
include(":analytics")
include(":uicentral")

// ISO8583 Contracts and MLE2 TCP Implementation
include(":pos", ":poscore")

// EMV Card Modules
include(":wizar", ":wizar_cloudpos_sdk")
include(":dspread")
include(":nexgo", ":nexgo_smartpos_sdk")
include(":telpo")
include(":sunmi", ":paylib")
include(":smartpeak")

buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
    remote<HttpBuildCache>() {
        isEnabled = false
    }
}