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
api = "http://51.15.86.64:8080"

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
                [ ("cap", Encode.float ps.cap)
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





initStrategyCreator: StrategyCreation
initStrategyCreator =
    { name = Initial
    , buySignalType =  Just AlwaysType
    , buyAboveCap = True
    , buyCap = Initial
    , buyTimeAmount = Initial
    , buySignalUp = Initial
    , sellSignalType= Just NeverType
    , sellAboveCap = False
    , sellCap = Initial
    , sellTimeAmount = Initial
    , sellSignalUp = Initial
    , buyStatus =  Initial
    , sellStatus =  Initial
    , selectorType = Nothing
    , selectorValue = Initial
    , visible = Modal.hiddenState
    , initialPrice = 0
    , triedSave = False}

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
                newName = if state == Modal.hiddenState then Initial
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
                command = if chosenSignalType /= AbsType then Cmd.none
                    else case model.strategyCreation.selectorValue of
                        Initial -> Cmd.none
                        ValidationError e -> Cmd.none
                        Valid v -> case v of
                            Single cTuple -> getInitialPriceForCompany cTuple.name
                            Sector sSel ->  getInitialPriceForSector <| sectorToString sSel
            in
                ({model | strategyCreation = newStr},command)
        SelectSelectorType selectType ->
            let
                chosenSelectorType = stringToSelectorType selectType
                initialChosenValue = if chosenSelectorType == SingleType then (case (List.head model.companies) of
                                                                            Nothing -> ValidationError "No companies found"
                                                                            Just head -> Valid <| Single head)
                    else Valid <| Sector <| stringToSector "Technology"

                command = case initialChosenValue of
                            Initial -> Cmd.none
                            ValidationError e -> Cmd.none
                            Valid v -> case v of
                                Single cTuple -> getInitialPriceForCompany cTuple.name
                                Sector sSel ->  getInitialPriceForSector <| sectorToString sSel

                oldStr = model.strategyCreation
                newStr = {oldStr | selectorType = Just chosenSelectorType, selectorValue = initialChosenValue}
            in
                ({model | strategyCreation = newStr},command)
        UpdateStrategyName chosenName ->
            let
                oldStr = model.strategyCreation
                newStr = {oldStr | name = Valid chosenName}
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
                validatedCap = case (stringToCap cap) of
                   Err e -> ValidationError e
                   Ok v -> Valid v
                oldStr = model.strategyCreation
                newStr = {oldStr | buyCap = validatedCap}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateSellCap cap->
            let
                validatedCap =  case (stringToCap cap) of
                    Err e -> ValidationError e
                    Ok v -> Valid v
                oldStr = model.strategyCreation
                newStr = {oldStr | sellCap = validatedCap}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateBuyTimeAmount timeAmount ->
            let
                validated = resultToValidation <| stringToAmount timeAmount
                oldStr = model.strategyCreation
                newStr = {oldStr | buyTimeAmount = validated}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateSellTimeAmount timeAmount->
            let
                validated = resultToValidation <| stringToAmount timeAmount
                oldStr = model.strategyCreation
                newStr = {oldStr | sellTimeAmount = validated}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateBuySignalUp upwards->
            let
                validated = trendStringsToBool upwards
                oldStr = model.strategyCreation
                newStr = {oldStr | buySignalUp = Valid validated}
            in
                ({model | strategyCreation = newStr}, Cmd.none)
        UpdateSellSignalUp upwards->
            let
                validated = trendStringsToBool upwards
                oldStr = model.strategyCreation
                newStr = {oldStr | sellSignalUp = Valid validated}
            in
                ({model | strategyCreation = newStr}, Cmd.none)

        UpdateSelectorValue value ->
            let
                (selector,cmd) = case model.strategyCreation.selectorType of
                        Nothing -> (ValidationError "No selectortype selected", Cmd.none)
                        Just selType ->
                            case selType of
                                SectorType ->
                                    let
                                        sel = Valid <| Sector <| stringToSector value
                                    in
                                        (sel, getInitialPriceForSector value)
                                SingleType ->
                                    let
                                        cTuple =  List.Extra.find (\val -> val.name == value) model.companies
                                    in
                                        case cTuple of
                                              Nothing -> (ValidationError "Cant find company with that name", Cmd.none) -- Should never happen
                                              Just t -> (Valid <| Single <| t, getInitialPriceForCompany t.symbol)  --safe lookup

                oldStr = model.strategyCreation
                newStr = {oldStr | selectorValue = selector}
            in
                ({model | strategyCreation = newStr}, cmd)

        SaveStrategy ->
            let
                selector = model.strategyCreation.selectorValue
                buySignal = createBuySignal model.strategyCreation
                sellSignal = createSellSignal model.strategyCreation
                maybeNewStrat = case (selector, buySignal, sellSignal) of
                    (Valid select, Valid bSig, Valid sSig) -> Just <| UserStrategy (straName select bSig sSig) bSig sSig select
                    (_,_,_) -> Nothing
                strategies = case maybeNewStrat of
                    Just str -> [str] ++ model.actualStrates
                    Nothing -> model.actualStrates
                strCreation = model.strategyCreation
                strCreationAfter = case maybeNewStrat of
                    Just str -> initStrategyCreator
                    Nothing -> {strCreation | triedSave = True}
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
        CompaniesResponse (Ok companyList) -> ({model | companies = List.take 100 companyList}, Cmd.none)
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

createBuySignal: StrategyCreation -> Validation Signal
createBuySignal model =
    case model.buySignalType of
        Nothing -> ValidationError "No signal type selected"
        Just signalType -> case signalType of
            AbsType -> case model.buyCap of
                Initial -> ValidationError "Choose a buy cap"
                ValidationError err -> ValidationError err
                Valid cap -> Valid <| Price <|PriceSignal cap model.buyAboveCap
            TrendType -> case (model.buyTimeAmount, model.buySignalUp) of
                (Valid timeAmount, Valid upwardsBool) -> Valid <| Trend <|TrendSignal timeAmount upwardsBool
                (_,_) -> ValidationError "Some error occured"
            NeverType -> Valid NeverMatch
            AlwaysType -> Valid AlwaysMatch

createSellSignal: StrategyCreation -> Validation Signal
createSellSignal model =
    case model.sellSignalType of
        Nothing -> ValidationError "No signal type selected"
        Just signalType -> case signalType of
            AbsType -> case model.sellCap of
                Initial -> ValidationError "Choose a sellcap"
                ValidationError err -> ValidationError err
                Valid cap -> Valid <| Price <|PriceSignal cap model.sellAboveCap
            TrendType -> case (model.sellTimeAmount, model.sellSignalUp) of
                (Valid timeAmount, Valid upwardsBool) -> Valid <| Trend <|TrendSignal timeAmount upwardsBool
                (_,_) -> ValidationError "Some error occured"
            NeverType -> Valid NeverMatch
            AlwaysType -> Valid AlwaysMatch