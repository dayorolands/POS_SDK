rootProject.name = "Cluster"

include(":app")
include(":core")
include(":analytics")
include(":uicentral")

// ISO8583 Contracts and MLE2 TCP Implementation
include(":pos", ":poscore")

// EMV Card Modules
//include(":dspread")
include(":sunmi")
include(":horizonpay")
include(":dspread")

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