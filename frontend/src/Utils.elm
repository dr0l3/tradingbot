module Utils exposing (..)
import Types exposing (..)

stringToBool: String -> Bool
stringToBool str =
    let
        b = if str == "Above" then True
            else False
    in
        b

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
