module Types exposing(..)

type alias PriceSignal =
    {   cap: Float
    ,   activeAboveCap: Bool}

timeUnits: List String
timeUnits = ["Day", "Week", "Month", "Year"]

type alias TrendSignal =
    {   days: Int
    ,   upwardsTrend: Bool}

type SignalType = AbsType | TrendType | NeverType | AlwaysType

signalTypesAsString: List String
signalTypesAsString = ["Based on Price", "Based on Trend", "Always", "Never"]

stringToSignalType: String -> SignalType
stringToSignalType str =
    if str == "Based on Price" then AbsType
        else if str == "Based on Trend" then TrendType
        else if str == "Always" then AlwaysType
        else NeverType

trendStrings: List String
trendStrings = ["Upwards", "Downwards"]

trendStringsToBool: String -> Bool
trendStringsToBool str =
    if str == "Upwards" then True
        else False

capBoolsToString: List String
capBoolsToString = ["Above price", "Below price"]

capStringToBools: String -> Bool
capStringToBools str =
    if str == "Above price" then True
        else False

type Signal = Price PriceSignal
    | Trend TrendSignal
    | NeverMatch
    | AlwaysMatch

type SelectorType = SectorType | SingleType

selectorTypesAsString: List String
selectorTypesAsString = ["Sector", "Single Company"]

stringToSelectorType: String -> SelectorType
stringToSelectorType str =
    if str == "Sector" then SectorType
        else SingleType

type SectorSelector = Technology
    | HealthCare
    | ConsumerServices
    | CapitalGoods
    | ConsumerDurables
    | Finance
    | Misc
    | ConsumerNonDurables
    | PublicUtilities
    | BasicIndustries
    | Energy
    | Transportation

sectorAsStrings: List String
sectorAsStrings =
    [ "Technology", "Health Care", "Consumer Services", "Capital Goods"
    , "Consumer Durables", "Finance", "Misc", "Consumer Non Durables"
    , "Public Utilities", "Basic Industries", "Energy", "Transportation"]

sectorToString: SectorSelector -> String
sectorToString sector =
    case sector of
        Technology -> "Technology"
        HealthCare -> "Health Care"
        ConsumerServices -> "Consumer Services"
        CapitalGoods -> "Capital Goods"
        ConsumerDurables -> "Consumer Durables"
        Finance -> "Finance"
        Misc -> "Misc"
        ConsumerNonDurables ->"Consumer Non Durables"
        PublicUtilities -> "Public Utilities"
        BasicIndustries -> "Basic Industries"
        Energy -> "Energy"
        Transportation -> "Transportation"

companySymbols: List String
companySymbols = ["FBHS", "PNI", "LYG", "PJC", "FOR", "TX", "LMT", "TNH", "LPX"
                 , "TVC", "FC", "LOW", "FTS", "LTC", "SCD", "TDC", "FNV","TEN"
                 , "MHI", "FTAI", "TEX", "FET", "LXU", "PNW", "FT", "PES", "TVE"
                 , "MAV", "THC", "DDD", "FSB", "PHD"]

stringToSector: String -> SectorSelector
stringToSector str =
    case str of
        "Technology" -> Technology
        "Health Care" -> HealthCare
        "Consumer Services" -> ConsumerServices
        "Capital Goods" -> CapitalGoods
        "Consumer Durables" -> ConsumerDurables
        "Finance" -> Finance
        "Misc" -> Misc
        "Consumer Non Durables" -> ConsumerNonDurables
        "Public Utilities" -> PublicUtilities
        "Basic Industries" -> BasicIndustries
        "Energy" -> Energy
        "Transportation"-> Transportation
        _ -> Misc

type alias SingleCompanySelector =
    { symbol: String}


type Selector = Single SingleCompanySelector | Sector SectorSelector



type alias UserStrategy =
    { name: String
    , buySignal: Signal
    , sellSignal: Signal
    , selector: Selector}


type alias UserState =
    { name: String
    , strategies: List UserStrategy}


type alias Error = String

type alias Dashboard =
    { progress: Float -- between 0 and 100
    , entries: List DashboardEntry}

type alias DashboardEntry =
    { userName: String
    , netWorth: Float}