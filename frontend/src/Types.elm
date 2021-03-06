module Types exposing(..)
import Bootstrap.Modal as Modal

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
signalTypesAsString = ["Always", "Never","Based on Price", "Based on Trend"]

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
    | ConsumerNonDurables
    | PublicUtilities
    | BasicIndustries

sectorAsStrings: List String
sectorAsStrings =
    [ "Technology", "Health Care", "Consumer Services", "Capital Goods"
    , "Consumer Durables", "Finance", "Consumer Non-Durables"
    , "Public Utilities", "Basic Industries"]

sectorToString: SectorSelector -> String
sectorToString sector =
    case sector of
        Technology -> "Technology"
        HealthCare -> "Health Care"
        ConsumerServices -> "Consumer Services"
        CapitalGoods -> "Capital Goods"
        ConsumerDurables -> "Consumer Durables"
        Finance -> "Finance"
        ConsumerNonDurables ->"Consumer Non-Durables"
        PublicUtilities -> "Public Utilities"
        BasicIndustries -> "Basic Industries"


stringToSector: String -> SectorSelector
stringToSector str =
    case str of
        "Technology" -> Technology
        "Health Care" -> HealthCare
        "Consumer Services" -> ConsumerServices
        "Capital Goods" -> CapitalGoods
        "Consumer Durables" -> ConsumerDurables
        "Finance" -> Finance
        "Consumer Non-Durables" -> ConsumerNonDurables
        "Public Utilities" -> PublicUtilities
        _ -> BasicIndustries


type Selector = Single CompanyTuple | Sector SectorSelector

type alias Email = String


type alias UserStrategy =
    { name: String
    , buySignal: Signal
    , sellSignal: Signal
    , selector: Selector}


type alias UserState =
    { name: String
    , email: String
    , strategies: List UserStrategy}


type alias Error = String

type alias Dashboard =
    { progress: Float -- between 0 and 100
    , entries: List DashboardEntry}

type alias DashboardEntry =
    { userName: String
    , netWorth: Float}

type alias InitialCompanyPrice =
    { symbol: String
    , price: Float}

type alias InitialSectorPrice =
    { symbol: String
    , price: Float}

type alias CompanyTuple =
    { symbol: String
    , name: String}

type Notification = Success String | Failure String

type alias StandardResponse =
    { status: Int
    , msg: String }

type Validation a = Initial | ValidationError String | Valid a


type alias StrategyCreation =
    { name: Validation String
    , buySignalType: Maybe SignalType
    , buyAboveCap: Bool
    , buyCap: Validation Float
    , buyTimeAmount: Validation Int
    , buySignalUp: Validation Bool
    , sellSignalType: Maybe(SignalType)
    , sellAboveCap: Bool
    , sellCap: Validation Float
    , sellTimeAmount: Validation Int
    , sellSignalUp: Validation Bool
    , buyStatus: Validation Signal
    , sellStatus: Validation Signal
    , selectorType: Maybe SelectorType
    , selectorValue: Validation Selector
    , visible: Modal.State
    , initialPrice: Float
    , triedSave: Bool
    }