port module TradingFrontend exposing (..)

import Html exposing (..)
import Types exposing (..)
import State exposing (..)
import Views exposing (..)
import Bootstrap.Dropdown as Dropdown


main =
    Html.program
        {   init = init
        ,   view = view
        ,   update = update
        ,   subscriptions = subscriptions}


subscriptions : Model -> Sub Msg
subscriptions model = Sub.none