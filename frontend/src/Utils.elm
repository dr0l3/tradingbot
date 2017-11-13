module Utils exposing (..)
import Types exposing (..)
import Bootstrap.Form as Form

stringToBool: String -> Result Error Bool
stringToBool str =
    if str == "Above" then Ok True
    else if str == "Below" then  Ok False
    else Err "Wrong bool value"

stringToCap: String -> Result String Float
stringToCap str =
    Result.andThen (\v -> isPositiveFloat v) (String.toFloat str)

isPositiveFloat: Float -> Result String Float
isPositiveFloat v =
    if v >= 0 then Ok v
        else Err "Must be non-negative"

isPositiveInt: Int -> Result String Int
isPositiveInt v =
    if v >= 0 then Ok v
        else Err "Must be non-negative"

isWithinPercentInterval: Float -> Result String Float
isWithinPercentInterval v =
    if (v >= 0 && v <= 100) then Ok v
        else Err "Must be within 0 and 100"

stringToAmount: String -> Result Error Int
stringToAmount str =
    Result.andThen (\v -> isPositiveInt v) (String.toInt str)

resultToValidation: Result Error a -> Validation a
resultToValidation res =
    case res of
        Err err -> ValidationError err
        Ok v -> Valid v


maybeToString: Maybe String -> String
maybeToString maybe =
    case maybe of
        Nothing -> ""
        Just v -> v


straName: Selector -> Signal -> Signal -> String
straName select buy sell =
    let
        selectName = selectorToName select
        buyName = signalToName buy
        sellName = signalToName sell
    in
        String.concat ["Buy ", selectName," ", buyName, " and sell ",sellName]

signalToName: Signal -> String
signalToName signal =
    case signal of
        Price p -> "based on price"
        Trend t -> "based on trend"
        NeverMatch-> "never"
        AlwaysMatch -> "always"

selectorToName: Selector -> String
selectorToName selector =
    case selector of
        Single single -> single.name
        Sector sector -> sectorToString sector

selectorTypeToInitialPriceText: Maybe SelectorType -> Float -> String
selectorTypeToInitialPriceText mb init=
    case mb of
        Nothing -> "No selector type chosen"
        Just sel -> case sel of
            SectorType -> String.concat ["Initial average prices is ", toString init]
            SingleType -> String.concat ["Initial price is ", toString init]

validToSuccessIndicator valid triedSave=
    case (valid, triedSave) of
        (Initial, False) -> []
        (Initial, True) -> [Form.groupDanger]
        (ValidationError e, _) -> [Form.groupDanger]
        (Valid v, _) -> [Form.groupSuccess]

validationToMessage: Validation a -> Bool -> String
validationToMessage validation triedSave=
    case (validation, triedSave) of
        (Initial, True) -> "This field needs a value"
        (Initial, False) -> ""
        (ValidationError err, _) -> err
        (Valid v, _) -> ""