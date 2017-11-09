module State exposing (..)
import Http
import List.Extra
import Process
import Task
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
api = "http://localhost:8082"

addUserUrl: String
addUserUrl = api ++ "/addUser"

delay: Time -> msg -> Cmd msg
delay time msg =
  Process.sleep time
  |> Task.andThen (always <| Task.succeed msg)
  |> Task.perform identity




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
        , ("email", Encode.string state.email)
        , ("strategies", Encode.list (List.map strategyEncoder state.strategies))]


standardResponseDecoder: Decoder StandardResponse
standardResponseDecoder = Pipe.decode StandardResponse
    |> Pipe.required "status" Decode.int
    |> Pipe.required "message" Decode.string

addNewUser: List(UserStrategy)-> String -> Email -> Cmd Msg
addNewUser strats name email =
    let
        user = UserState name email strats
        body = userStateEncoder user |> Http.jsonBody
        req =Http.post addUserUrl body standardResponseDecoder
    in
        Http.send StrategySubmissionResponse req

dbEntryDecoder: Decoder DashboardEntry
dbEntryDecoder = Pipe.decode DashboardEntry
    |> Pipe.required "userName" Decode.string
    |> Pipe.required "netWorth" Decode.float

dashboardDecoder: Decoder Dashboard
dashboardDecoder = Pipe.decode Dashboard
    |> Pipe.required "progressInPercent" Decode.float
    |> Pipe.required "entries" (Decode.list dbEntryDecoder)

companyPriceDecoder: Decoder InitialCompanyPrice
companyPriceDecoder = Pipe.decode InitialCompanyPrice
    |> Pipe.required "symbol" Decode.string
    |> Pipe.required "price" Decode.float

sectorPriceDecoder: Decoder InitialSectorPrice
sectorPriceDecoder = Pipe.decode InitialSectorPrice
    |> Pipe.required "sector" Decode.string
    |> Pipe.required "price" Decode.float

companyTupleDecoder: Decoder CompanyTuple
companyTupleDecoder = Pipe.decode CompanyTuple
    |> Pipe.required "symbol" Decode.string
    |> Pipe.required "name" Decode.string

refreshDashboard: Cmd Msg
refreshDashboard =
    let
        dashboardURL = api ++ "/dashboard"
        dbReq = Http.get dashboardURL dashboardDecoder
    in
        Http.send DashboardResponse dbReq

getAllCompanies: Cmd Msg
getAllCompanies =
    let
        companiesURL = api ++ "/allCompanies"
        req = Http.get companiesURL (Decode.list companyTupleDecoder)
    in
        Http.send CompaniesResponse req

getInitialPriceForCompany: String -> Cmd Msg
getInitialPriceForCompany symbol =
    let
        url = api ++ "/initialPriceCompany/" ++ symbol
        req = Http.get url companyPriceDecoder
    in
        Http.send InitialCompanyResponse req

getInitialPriceForSector: String -> Cmd Msg
getInitialPriceForSector sectorName =
    let
        url = api ++ "/initialPriceSector/" ++ sectorName
        req = Http.get url sectorPriceDecoder
    in
        Http.send InitialSectorResponse req

type alias Model =
    { print: String
    , actualStrates: List UserStrategy
    , strategyCreation: StrategyCreation
    , dashboard: Dashboard
    , userName: String
    , response: String
    , tabstate: Tab.State
    , notifications: List(Notification)
    , companies: List(CompanyTuple)
    , email: Maybe String
    , submitted: Bool}



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
    , initialPrice: Float
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
    , visible = Modal.hiddenState
    , initialPrice = 0}

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
    | StrategySubmissionResponse (Result Http.Error StandardResponse)
    | RefreshDashboard Time
    | DashboardResponse(Result Http.Error Dashboard)
    | CompaniesResponse(Result Http.Error (List(CompanyTuple)))
    | InitialCompanyResponse(Result Http.Error InitialCompanyPrice)
    | InitialSectorResponse(Result Http.Error InitialSectorPrice)
    | UpdateEmail String
    | RemoveOldestError
    | DeleteStrategyByName String


initialDashboard: Dashboard
initialDashboard = Dashboard 0 []

init: (Model, Cmd Msg)
init = (Model "hello" [] initStrategyCreator initialDashboard "" "" Tab.initialState [] [] Nothing False, Cmd.none)

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
                command = if List.isEmpty model.companies then getAllCompanies
                    else Cmd.none
            in
                ({model | strategyCreation = newStr}, command)
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
                initialChosenValue = if chosenSelectorType == SingleType then (case (List.head model.companies) of
                                                                            Nothing -> Err "No companies found"
                                                                            Just head -> Ok <| Single head)
                    else Ok <| Sector <| stringToSector "Technology"
                oldStr = model.strategyCreation
                newStr = {oldStr | selectorType = Just chosenSelectorType, selectorValue = initialChosenValue}
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
                (selector,cmd) = case model.strategyCreation.selectorType of
                        Nothing -> (Err "No selectortype selected", Cmd.none)
                        Just selType ->
                            case selType of
                                SectorType ->
                                    let
                                        sel = Ok <| Sector <| stringToSector value
                                    in
                                        (sel, getInitialPriceForSector value)
                                SingleType ->
                                    let
                                        cTuple =  List.Extra.find (\val -> val.name == value) model.companies
                                    in
                                        case cTuple of
                                              Nothing -> (Err "Cant find company with that name", Cmd.none) -- Should never happen
                                              Just t -> (Ok <| Single <| t, getInitialPriceForCompany t.symbol)  --safe lookup

                oldStr = model.strategyCreation
                newStr = {oldStr | selectorValue = selector}
            in
                ({model | strategyCreation = newStr}, cmd)

        SaveStrategy ->
            let
                selector = model.strategyCreation.selectorValue
                buySignal = createBuySignal model.strategyCreation
                sellSignal = createSellSignal model.strategyCreation
                prio = model.strategyCreation.priority
                maybeNewStrat = case (selector, buySignal, sellSignal) of
                    (Ok select, Ok bSig, Ok sSig) -> Just <| UserStrategy (straName select bSig sSig) bSig sSig select
                    (_,_,_) -> Nothing
                strategies = case maybeNewStrat of
                    Just str -> [str] ++ model.actualStrates
                    Nothing -> model.actualStrates
                strCreationAfter = case maybeNewStrat of
                    Just str -> initStrategyCreator
                    Nothing -> model.strategyCreation
            in
                ({model | actualStrates = strategies, strategyCreation = strCreationAfter}, Cmd.none)
        SubmitStrategies ->
            let
                (newModel,cmd) = case (model.email, String.length model.userName) of
                    (Nothing,_) ->
                        ( {model| notifications = errorFromString "Please add your email" :: model.notifications}
                        , delay (Time.second * 5) <| RemoveOldestError)
                    (Just _, 0) ->
                        ( {model| notifications = errorFromString "Name not long enough" :: model.notifications}
                        , delay (Time.second * 5) <| RemoveOldestError)
                    (Just _, 1) ->
                        ( {model| notifications = errorFromString "Name not long enough" :: model.notifications}
                        , delay (Time.second * 5) <| RemoveOldestError)
                    (Just email,_) -> (model, addNewUser model.actualStrates model.userName email)
            in
                (newModel, cmd)
        StrategySubmissionResponse resp ->
            let
                (res, submitSuccess) = case resp of
                        Err e -> (errorFromString "Unable to submit strategy", False)
                        Ok r -> if r.status == 200 then (successFromString "Successfully submitted strategy", True)
                            else (errorFromString r.msg, False)

            in
             ({model | notifications = res :: model.notifications, submitted = submitSuccess}, delay (Time.second * 5) <| RemoveOldestError)
        RefreshDashboard _ -> (model, refreshDashboard)
        DashboardResponse (Ok dn) -> ({model | dashboard = dn}, Cmd.none)
        DashboardResponse (Err err) ->
            ({model | notifications = (errorFromString "Unable to refresh dashboard"):: model.notifications}, delay (Time.second * 5) <| RemoveOldestError)
        CompaniesResponse (Ok companyList) -> ({model | companies = (List.take 100 companyList)}, Cmd.none)
        CompaniesResponse (Err err) ->
            ({model | notifications = (errorFromString "Unable to fetch list of companies") :: model.notifications}, delay (Time.second * 5) <| RemoveOldestError)
        InitialCompanyResponse (Ok initialPrice) ->
            let
                price = initialPrice.price
                oldStr = model.strategyCreation
                newStr = {oldStr | initialPrice = price}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        InitialCompanyResponse (Err err) ->
            ({model | notifications = (errorFromString "Unable to fetch initial company price") :: model.notifications}, delay (Time.second * 5) <| RemoveOldestError)
        InitialSectorResponse (Ok initialPrice) ->
            let
                price = initialPrice.price
                oldStr = model.strategyCreation
                newStr = {oldStr | initialPrice = price}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        InitialSectorResponse (Err err) ->
            ({model | notifications = (errorFromString "Unable to fetch initial sector price") :: model.notifications}, delay (Time.second * 5) <| RemoveOldestError)
        RemoveOldestError ->
            let
                newnots = List.reverse <| case  List.tail <| List.reverse model.notifications of
                    Just list -> list
                    Nothing -> []

            in
                ({model | notifications = newnots}, Cmd.none)
        UpdateEmail string -> ({model | email = Just string}, Cmd.none)
        DeleteStrategyByName name ->
            let
                res = List.filter (\s -> s.name /= name) model.actualStrates
            in
                ({model | actualStrates = res}, Cmd.none)


errorFromString: String -> Notification
errorFromString msg =
    Failure msg

successFromString: String -> Notification
successFromString msg =
    Success msg

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