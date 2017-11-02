module State exposing (..)
import Types exposing (..)
import Utils exposing (..)
import Bootstrap.Dropdown as Dropdown
import Bootstrap.Modal as Modal

type alias Model =
    { print: String
    , strats: List UserStrategy
    , actualStrates: List UserStrategy
    , strategyCreation: StrategyCreation}



type alias StrategyCreation =
    { name: Maybe String
    , buySignalType: Maybe SignalType
    , buyAboveCap: Bool
    , buyCap: Result Error Float
    , buyTimeUnit: Maybe(String)
    , buyTimeAmount: Result Error Int
    , buyPercentage: Result Error Float
    , sellSignalType: Maybe(SignalType)
    , sellAboveCap: Bool
    , sellCap: Result Error Float
    , sellTimeUnit: Maybe(String)
    , sellTimeAmount: Result Error Int
    , sellPercentage: Result Error Float
    , priority: Int
    , percentage: Result Error Float
    , buyStatus: Result Error Signal
    , sellStatus: Result Error Signal
    , selectorType: Maybe SelectorType
    , selectorValue: Result Error Selector
    , visible: Modal.State
    }

initStrategyCreator: StrategyCreation
initStrategyCreator =
    { name = Nothing
    , buySignalType =  Just AlwaysType
    , buyAboveCap = True
    , buyCap = Ok 0
    , buyTimeUnit = Nothing
    , buyTimeAmount = Ok 0
    , buyPercentage = Ok 0
    , sellSignalType= Just NeverType
    , sellAboveCap = False
    , sellCap = Ok 0
    , sellTimeUnit = Nothing
    , sellTimeAmount = Ok 0
    , sellPercentage = Ok 0
    , priority= 5
    , percentage= Err "Initial"
    , buyStatus =  Err "Initial"
    , sellStatus =  Err "Initial"
    , selectorType = Nothing
    , selectorValue = Err "initial"
    , visible = Modal.hiddenState }

type Msg = NoOp
    | SelectBuySignalType(String)
    | SelectSellSignalType(String)
    | SelectSelectorType(String)
    | DisplayStrategyModal Modal.State
    | UpdateName(String)
    | UpdateBuyAboveCap(String)
    | UpdateBuyCap(String)
    | UpdateBuyTimeUnit(String)
    | UpdateBuyTimeAmount(String)
    | UpdateBuyPercentage(String)
    | UpdateSellAboveCap(String)
    | UpdateSellCap(String)
    | UpdateSellTimeUnit(String)
    | UpdateSellTimeAmount(String)
    | UpdateSellPercentage(String)
    | UpdateSelectorValue(String)
    | UpdatePriority(String)
    | UpdatePercentage(String)
    | SubmitStrategy

str1 : UserStrategy
str1 = UserStrategy "LYG IS THE BEST" (Absolute (AbsoluteValueSignal 1.00 False)) (Absolute (AbsoluteValueSignal 2.0 True)) (Single (SingleCompanySelector ("LYG"))) 1 0.2


str2 : UserStrategy
str2 = UserStrategy "THN rocks" (Absolute (AbsoluteValueSignal 10.00 False)) (Absolute (AbsoluteValueSignal 20.3 True)) (Single (SingleCompanySelector ("TNH"))) 1 0.2


init: (Model, Cmd Msg)
init = (Model "hello" [str1, str2] [] initStrategyCreator, Cmd.none)

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        NoOp -> (model, Cmd.none)
        DisplayStrategyModal state ->
            let
                oldStr = model.strategyCreation
                newStr = {oldStr | visible = state}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        SelectBuySignalType string ->
            let
                chosenSignalType = stringToSignalType string
                oldStr = model.strategyCreation
                newStr = {oldStr | buySignalType = Just chosenSignalType}
            in
                ({model | strategyCreation = newStr},Cmd.none)
        SelectSellSignalType string ->
            let
                chosenSignalType = stringToSignalType string
                oldStr = model.strategyCreation
                newStr = {oldStr | sellSignalType = Just chosenSignalType}
            in
                ({model | strategyCreation = newStr},Cmd.none)
        SelectSelectorType selectType ->
            let
                chosenSelectorType = stringToSelectorType selectType
                oldStr = model.strategyCreation
                newStr = {oldStr | selectorType = Just chosenSelectorType}
            in
                ({model | strategyCreation = newStr},Cmd.none)
        UpdateName chosenName ->
            let
                oldStr = model.strategyCreation
                newStr = {oldStr | name = Just chosenName}
            in
                ({model | strategyCreation = newStr},Cmd.none)

        UpdateBuyAboveCap string ->
            let
                oldStr = model.strategyCreation
                newStr = {oldStr | buyAboveCap = capStringToBools string}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateSellAboveCap string->
            let
                oldStr = model.strategyCreation
                newStr = {oldStr | sellAboveCap = capStringToBools string}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateBuyCap cap ->
            let
                validatedCap = stringToCap cap
                oldStr = model.strategyCreation
                newStr = {oldStr | buyCap = validatedCap}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateSellCap cap->
            let
                validatedCap = stringToCap cap
                oldStr = model.strategyCreation
                newStr = {oldStr | sellCap = validatedCap}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateBuyTimeUnit timeUnit ->
            let
                oldStr = model.strategyCreation
                newStr = {oldStr | buyTimeUnit = Just timeUnit}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateSellTimeUnit timeUnit->
            let
                oldStr = model.strategyCreation
                newStr = {oldStr | sellTimeUnit = Just timeUnit}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateBuyTimeAmount timeAmount ->
            let
                validated = stringToAmount timeAmount
                oldStr = model.strategyCreation
                newStr = {oldStr | buyTimeAmount = validated}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateSellTimeAmount timeAmount->
            let
                validated = stringToAmount timeAmount
                oldStr = model.strategyCreation
                newStr = {oldStr | sellTimeAmount = validated}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateBuyPercentage percent->
            let
                validated = Result.andThen isWithinPercentInterval (String.toFloat percent)
                oldStr = model.strategyCreation
                newStr = {oldStr | buyPercentage = validated}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateSellPercentage percent->
            let
                validated = Result.andThen isWithinPercentInterval (String.toFloat percent)
                oldStr = model.strategyCreation
                newStr = {oldStr | sellPercentage = validated}
            in
                ({model | strategyCreation = newStr}, Cmd.none)

        UpdateSelectorValue value ->
            let
                selector = case model.strategyCreation.selectorType of
                        Nothing -> Err "No selectortype selected"
                        Just selType ->
                            case selType of
                                SingleType -> Ok <| Single <| SingleCompanySelector value --safe lookup
                                SectorType -> Ok <| Sector <| stringToSector value
                oldStr = model.strategyCreation
                newStr = {oldStr | selectorValue = selector}
            in
                ({model | strategyCreation = newStr}, Cmd.none)

        UpdatePercentage value ->
            let
                percentage = String.toFloat value
                oldStr = model.strategyCreation
                newStr = {oldStr | percentage = percentage}
            in
                ({model | strategyCreation = newStr}, Cmd.none)

        UpdatePriority value ->
            let
                prio = Result.withDefault 5 <| String.toInt value
                oldStr = model.strategyCreation
                newStr = {oldStr | priority = prio}
            in
                ({model | strategyCreation = newStr}, Cmd.none)

        SubmitStrategy ->
            let
                selector = model.strategyCreation.selectorValue
                buySignal = createBuySignal model.strategyCreation
                sellSignal = createSellSignal model.strategyCreation
                prio = model.strategyCreation.priority
                maybeNewStrat = case (selector, buySignal, sellSignal, model.strategyCreation.name, model.strategyCreation.percentage) of
                    (Ok select, Ok bSig, Ok sSig, Just name, Ok percent) -> Just <| UserStrategy name bSig sSig select prio percent
                    (_,_,_,_,_) -> Nothing
                strategies = case maybeNewStrat of
                    Just str -> [str] ++ model.actualStrates
                    Nothing -> model.actualStrates
                strCreationAfter = case maybeNewStrat of
                    Just str -> initStrategyCreator
                    Nothing -> model.strategyCreation
            in
                ({model | actualStrates = strategies, strategyCreation = strCreationAfter}, Cmd.none)


createBuySignal: StrategyCreation -> Result Error Signal
createBuySignal model =
    case model.buySignalType of
        Nothing -> Err "No signal type selected"
        Just signalType -> case signalType of
            AbsType -> case model.buyCap of
                Err err -> Err err
                Ok cap -> Ok <| Absolute <|AbsoluteValueSignal cap model.buyAboveCap
            TrendType -> case (model.buyTimeUnit, model.buyTimeAmount, model.buyPercentage) of
                (Just timeUnit, Ok timeAmount, Ok buyPercentage) -> Ok <| Trend <|TrendSignal timeUnit timeAmount buyPercentage
                (_,_,_) -> Err "Some error occured"
            NeverType -> Ok NeverMatch
            AlwaysType -> Ok AlwaysMatch

createSellSignal: StrategyCreation -> Result Error Signal
createSellSignal model =
    case model.sellSignalType of
        Nothing -> Err "No signal type selected"
        Just signalType -> case signalType of
            AbsType -> case model.sellCap of
                Err err -> Err err
                Ok cap -> Ok <| Absolute <|AbsoluteValueSignal cap model.sellAboveCap
            TrendType -> case (model.sellTimeUnit, model.sellTimeAmount, model.sellPercentage) of
                (Just timeUnit, Ok timeAmount, Ok buyPercentage) -> Ok <| Trend <|TrendSignal timeUnit timeAmount buyPercentage
                (_,_,_) -> Err "Some error occured"
            NeverType -> Ok NeverMatch
            AlwaysType -> Ok AlwaysMatch