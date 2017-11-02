module Types exposing(..)

type alias AbsoluteValueSignal =
    {   cap: Float
    ,   activeAboveCap: Bool}

timeUnits: List String
timeUnits = ["Day", "Week", "Month", "Year"]

type alias TrendSignal =
    {   timeUnit: String
    ,   timeAmount: Int
    ,   percentage: Float}

type SignalType = AbsType | TrendType | NeverType | AlwaysType

signalTypesAsString: List String
signalTypesAsString = ["Absolute", "Trend", "Always", "Never"]

stringToSignalType: String -> SignalType
stringToSignalType str =
    if str == "Absolute" then AbsType
        else if str == "Trend" then TrendType
        else if str == "Always" then AlwaysType
        else NeverType

capBoolsToString: List String
capBoolsToString = ["Above cap", "Below cap"]

capStringToBools: String -> Bool
capStringToBools str =
    if str == "Above cap" then True
        else False

type Signal = Absolute AbsoluteValueSignal
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
    , selector: Selector
    , priority: Int
    , percentage: Float}


type alias Error = String