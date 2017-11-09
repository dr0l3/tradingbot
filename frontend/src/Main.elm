port module TradingFrontend exposing (..)

import Html exposing (..)
import Types exposing (..)
import State exposing (..)
import Views exposing (..)
import Bootstrap.Dropdown as Dropdown
import Time exposing (..)


main =
    Html.program
        {   init = init
        ,   view = view
        ,   update = update
        ,   subscriptions = subscriptions}


subscriptions : Model -> Sub Msg
subscriptions model = Sub.none
--subscriptions model = Time.every (5 * Time.second) RefreshDashboard