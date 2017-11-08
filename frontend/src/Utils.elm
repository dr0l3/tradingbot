module Utils exposing (..)
import Types exposing (..)

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


maybeToString: Maybe String -> String
maybeToString maybe =
    case maybe of
        Nothing -> ""
        Just v -> v