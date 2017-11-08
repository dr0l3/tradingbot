module State exposing (..)
import Http
import Types exposing (..)
import Utils exposing (..)
import Bootstrap.Dropdown as Dropdown
import Bootstrap.Modal as Modal
import Bootstrap.Tab as Tab

import Json.Decode as Decode exposing (..)
import Json.Decode.Pipeline as Pipe exposing (..)
import Json.Encode as Encode exposing (..)
import Result
import Time exposing (Time)

api: String
api = "http://localhost:8082/"

addUserUrl: String
addUserUrl = api ++ "addUser"


signalEncoder: Signal -> Encode.Value
signalEncoder signal =
    case signal of
        Price ps -> Encode.object
            [ ("type", Encode.string "model.signals.PriceSignal")
            , ("data", Encode.object
                [ ("price", Encode.float ps.cap)
                , ("activeAboveCap", Encode.bool ps.activeAboveCap)
                ])
            ]
        Trend ts -> Encode.object
            [ ("type", Encode.string "model.signals.TrendSignal")
            , ("data", Encode.object
                [ ("days", Encode.int ts.days)
                , ("upwardsTrend", Encode.bool ts.upwardsTrend)
                ])
            ]
        NeverMatch -> Encode.object
            [ ("type", Encode.string "model.signals.ConstantSignal")
            , ("data", Encode.object
                [("isAlwaysActive", Encode.bool False)
                ])
            ]
        AlwaysMatch -> Encode.object
            [ ("type", Encode.string "model.signals.ConstantSignal")
            , ("data", Encode.object
                [("isAlwaysActive", Encode.bool True)
                ])
            ]

selectorEncoder: Selector -> Encode.Value
selectorEncoder selector =
    case selector of
        Single scs -> Encode.object
            [ ("type", Encode.string "model.selectors.SingleCompanySelector")
            , ("data", Encode.object
                [("symbol", Encode.string scs.symbol)])]
        Sector ss -> Encode.object
            [ ("type", Encode.string "model.selectors.SectorSelector")
            , ("data", Encode.object
                [("sectorName", Encode.string (toString ss))])]

strategyEncoder: UserStrategy -> Encode.Value
strategyEncoder userStrategy =
    Encode.object
        [ ("selector", selectorEncoder userStrategy.selector)
        , ("buySignal", signalEncoder userStrategy.buySignal)
        , ("sellSignal", signalEncoder userStrategy.sellSignal)]

userStateEncoder: UserState -> Encode.Value
userStateEncoder state =
    Encode.object
        [ ("name", Encode.string state.name)
        , ("strategies", Encode.list (List.map strategyEncoder state.strategies))]

addNewUserRequest: Model -> Http.Request String
addNewUserRequest model =
    let
        strats = model.actualStrates
        user = UserState (model.userName) strats
        body = userStateEncoder user |> Http.jsonBody
    in
        Http.post addUserUrl body Decode.string

addNewUser: Model -> Cmd Msg
addNewUser model =
    Http.send StrategySubmissionResponse <| addNewUserRequest model

dbEntryDecoder: Decoder DashboardEntry
dbEntryDecoder = Pipe.decode DashboardEntry
    |> Pipe.required "userName" Decode.string
    |> Pipe.required "netWorth" Decode.float

dashboardDecoder: Decoder Dashboard
dashboardDecoder = Pipe.decode Dashboard
    |> Pipe.required "progressInPercent" Decode.float
    |> Pipe.required "entries" (Decode.list dbEntryDecoder)


refreshDashboard: Cmd Msg
refreshDashboard =
    let
        dashboardURL = api ++ "/dashboard"
        dbReq = Http.get dashboardURL dashboardDecoder
    in
        Http.send DashboardResponse dbReq

type alias Model =
    { print: String
    , actualStrates: List UserStrategy
    , strategyCreation: StrategyCreation
    , dashboard: Dashboard
    , userName: String
    , response: String
    , tabstate: Tab.State}



type alias StrategyCreation =
    { name: Maybe String
    , buySignalType: Maybe SignalType
    , buyAboveCap: Bool
    , buyCap: Result Error Float
    , buyTimeAmount: Result Error Int
    , buySignalUp: Result Error Bool
    , sellSignalType: Maybe(SignalType)
    , sellAboveCap: Bool
    , sellCap: Result Error Float
    , sellTimeAmount: Result Error Int
    , sellSignalUp: Result Error Bool
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
    , buyTimeAmount = Ok 0
    , buySignalUp = Err "Initial"
    , sellSignalType= Just NeverType
    , sellAboveCap = False
    , sellCap = Ok 0
    , sellTimeAmount = Ok 0
    , sellSignalUp = Err "Initial"
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
    | TabMsg(Tab.State)
    | UpdateName String
    | UpdateStrategyName(String)
    | UpdateBuyAboveCap(String)
    | UpdateBuyCap(String)
    | UpdateBuyTimeAmount(String)
    | UpdateBuySignalUp(String)
    | UpdateSellAboveCap(String)
    | UpdateSellCap(String)
    | UpdateSellTimeAmount(String)
    | UpdateSellSignalUp(String)
    | UpdateSelectorValue(String)
    | SaveStrategy
    | SubmitStrategies
    | StrategySubmissionResponse (Result Http.Error String)
    | RefreshDashboard Time
    | DashboardResponse(Result Http.Error Dashboard)

str1 : UserStrategy
str1 = UserStrategy "LYG IS THE BEST" (Price (PriceSignal 1.00 False)) (Price (PriceSignal 2.0 True)) (Single (SingleCompanySelector ("LYG")))


str2 : UserStrategy
str2 = UserStrategy "THN rocks" (Price (PriceSignal 10.00 False)) (Price (PriceSignal 20.3 True)) (Single (SingleCompanySelector ("TNH")))

initialDashboard: Dashboard
initialDashboard = Dashboard 0 []

init: (Model, Cmd Msg)
init = (Model "hello" [] initStrategyCreator initialDashboard "" "" Tab.initialState, Cmd.none)

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        NoOp -> (model, Cmd.none)
        DisplayStrategyModal state ->
            let
                oldStr = model.strategyCreation
                newName = if state == Modal.hiddenState then Nothing
                    else model.strategyCreation.name
                newStr = {oldStr | visible = state, name = newName}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        TabMsg state -> ({model | tabstate = state}, Cmd.none)
        UpdateName name -> ({model | userName = name}, Cmd.none)
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
        UpdateStrategyName chosenName ->
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
        UpdateBuySignalUp upwards->
            let
                validated = trendStringsToBool upwards
                oldStr = model.strategyCreation
                newStr = {oldStr | buySignalUp = Ok validated}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateSellSignalUp upwards->
            let
                validated = trendStringsToBool upwards
                oldStr = model.strategyCreation
                newStr = {oldStr | sellSignalUp = Ok validated}
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

        SaveStrategy ->
            let
                selector = model.strategyCreation.selectorValue
                buySignal = createBuySignal model.strategyCreation
                sellSignal = createSellSignal model.strategyCreation
                prio = model.strategyCreation.priority
                maybeNewStrat = case (selector, buySignal, sellSignal, model.strategyCreation.name) of
                    (Ok select, Ok bSig, Ok sSig, Just name) -> Just <| UserStrategy name bSig sSig select
                    (_,_,_,_) -> Nothing
                strategies = case maybeNewStrat of
                    Just str -> [str] ++ model.actualStrates
                    Nothing -> model.actualStrates
                strCreationAfter = case maybeNewStrat of
                    Just str -> initStrategyCreator
                    Nothing -> model.strategyCreation
            in
                ({model | actualStrates = strategies, strategyCreation = strCreationAfter}, Cmd.none)
        SubmitStrategies -> (model, addNewUser model)
        StrategySubmissionResponse resp ->
            let
                res = case resp of
                        Err e -> toString e
                        Ok r -> r

            in
             ({model | response = res}, Cmd.none)
        RefreshDashboard _ -> (model, refreshDashboard)
        DashboardResponse (Ok dn) -> ({model | dashboard = dn}, Cmd.none)
        DashboardResponse (Err err) -> ({model | response = (toString err)}, Cmd.none)




createBuySignal: StrategyCreation -> Result Error Signal
createBuySignal model =
    case model.buySignalType of
        Nothing -> Err "No signal type selected"
        Just signalType -> case signalType of
            AbsType -> case model.buyCap of
                Err err -> Err err
                Ok cap -> Ok <| Price <|PriceSignal cap model.buyAboveCap
            TrendType -> case (model.buyTimeAmount, model.buySignalUp) of
                (Ok timeAmount, Ok upwardsBool) -> Ok <| Trend <|TrendSignal timeAmount upwardsBool
                (_,_) -> Err "Some error occured"
            NeverType -> Ok NeverMatch
            AlwaysType -> Ok AlwaysMatch

createSellSignal: StrategyCreation -> Result Error Signal
createSellSignal model =
    case model.sellSignalType of
        Nothing -> Err "No signal type selected"
        Just signalType -> case signalType of
            AbsType -> case model.sellCap of
                Err err -> Err err
                Ok cap -> Ok <| Price <|PriceSignal cap model.sellAboveCap
            TrendType -> case (model.sellTimeAmount, model.sellSignalUp) of
                (Ok timeAmount, Ok upwardsBool) -> Ok <| Trend <|TrendSignal timeAmount upwardsBool
                (_,_) -> Err "Some error occured"
            NeverType -> Ok NeverMatch
            AlwaysType -> Ok AlwaysMatch