module Views exposing (..)

import Bootstrap.Grid.Row as Row
import Html.Events exposing (onClick)
import Types exposing (..)
import Html.Attributes exposing (..)
import State exposing (..)
import Html exposing (..)
import Bootstrap.Grid as Grid exposing (Column)
import Bootstrap.CDN as CDN
import Bootstrap.Button as Button
import Bootstrap.Dropdown as Dropdown
import Bootstrap.Form as Form
import Bootstrap.Form.Select as Select
import Bootstrap.Modal as Modal
import Bootstrap.Form.Input as Input
import Bootstrap.Form.Radio as Radio



strategyRow: UserStrategy -> Html msg
strategyRow str =
    Grid.row []
        [ Grid.col [] [ text str.name]
        , Grid.col []
            [ Button.button [] [ text "Details"]]
        , Grid.col []
            [ Button.button [] [ text "Edit"]]]


createBot: Model -> Html Msg
createBot model =
    Grid.container []
        ([ Grid.row []
            [ Grid.col [] [text "Create Bot"]]
        , Grid.row []
            [ Grid.col [] [text "Strategies"]]]
        ++ List.map strategyRow model.strats ++
        [ Grid.row []
            [ Grid.col [] [Button.button [] [text "Add another"]]]
        , Grid.row []
            [ Grid.col [] [Button.button [] [text "Submit"]]]])

signalSelect: Model -> (String -> Msg) -> Html Msg
signalSelect model msg =
    Select.select [ Select.onInput msg]
        [ Select.item [] [text "Value"]
        , Select.item [] [text "Trend"]]

textToItem: String -> Select.Item Msg
textToItem str =
    Select.item [] [text str]

textToRadioButton: (String -> Msg) -> String ->  Radio.Radio Msg
textToRadioButton msg str =
    Radio.createCustom [Radio.onClick <| (msg str)] str


strategyForm: Model -> Html.Html Msg
strategyForm model =
    let
        hidden = Row.attrs <| [style [("display", "none")]]
        visible = Row.attrs []

        displaySelectSectorGroup = case model.strategyCreation.selectorType of
            Nothing -> hidden
            Just selType -> case selType of
                SectorType -> visible
                _ -> hidden

        displaySelectCompanyGroup = case model.strategyCreation.selectorType of
            Nothing -> hidden
            Just selType -> case selType of
                SingleType -> visible
                _ -> hidden

        displayAbsBuySignalGroup = case model.strategyCreation.buySignalType of
            Nothing -> hidden
            Just sigType -> case sigType of
                AbsType -> visible
                _ -> hidden

        displayTrendBuySignalGroup = case model.strategyCreation.buySignalType of
            Nothing -> hidden
            Just sigType -> case sigType of
                TrendType -> visible
                _ -> hidden

        displayAbsSellSignalGroup = case model.strategyCreation.sellSignalType of
            Nothing -> hidden
            Just sigType -> case sigType of
                AbsType -> visible
                _ -> hidden

        displayTrendSellSignalGroup = case model.strategyCreation.sellSignalType of
            Nothing -> hidden
            Just sigType -> case sigType of
                TrendType -> visible
                _ -> hidden


    in
        Form.form []
            -- NAME
            [ Form.row []
                [ Form.colLabel [] [text "Name"]
                , Form.col []
                    [ Input.text [Input.onInput UpdateName]]]
            -- SELECTOR
            , Form.row []
                [ Form.colLabel [] [text "Selector Type"]
                , Form.col [] <| Radio.radioList "selectorTypeRadio" <| List.map (textToRadioButton SelectSelectorType) selectorTypesAsString
                ]
            , Form.row [displaySelectSectorGroup]
                [ Form.colLabel [] [text "Select a sector"]
                , Form.col []
                    [Select.custom [Select.onInput UpdateSelectorValue] (List.map textToItem sectorAsStrings)]]
            , Form.row [displaySelectCompanyGroup]
                [ Form.colLabel [] [text "Select a company"]
                , Form.col []
                    [Select.custom [Select.onInput UpdateSelectorValue] (List.map textToItem companySymbols)]]
            -- BUY SIGNAL
            , Form.row []
                [ Form.colLabel [] [text "BuySignalType"]
                , Form.col []
                    [ Select.custom [Select.id "buySignalType",Select.onInput SelectBuySignalType] (List.map textToItem (signalTypesAsString))]
                ]
            , Form.row [displayAbsBuySignalGroup]
                [ Form.colLabel [] [text "When is signal active?"]
                , Form.col []
                    [ Select.custom [Select.onInput UpdateBuyAboveCap] (List.map textToItem (capBoolsToString))]
                ]
            , Form.row [displayAbsBuySignalGroup]
                [ Form.colLabel [] [text "Cap"]
                , Form.col []
                    [Input.number [Input.onInput UpdateBuyCap]]]
            , Form.row [displayTrendBuySignalGroup]
                [ Form.colLabel [] [text "Timeunit"]
                , Form.col [] <| Radio.radioList "select timeunit" <| List.map (textToRadioButton UpdateBuyTimeUnit) timeUnits]
            , Form.row [displayTrendBuySignalGroup]
                [ Form.colLabel [] [text "Time Amount"]
                , Form.col []
                    [ Input.number [Input.onInput UpdateBuyTimeAmount]]]
            , Form.row [displayTrendBuySignalGroup]
                [ Form.colLabel [] [text "Percentage"]
                , Form.col []
                    [ Input.number [Input.onInput UpdateBuyPercentage]]]
            -- SELL SIGNAL
            , Form.row []
                [ Form.colLabel [] [text "SellSignalType"]
                , Form.col []
                    [ Select.custom [Select.id "sellSignalType",Select.onInput SelectSellSignalType] (List.map textToItem (signalTypesAsString))]
                ]
            , Form.row [displayAbsSellSignalGroup]
                [ Form.colLabel [] [text "When is signal active?"]
                , Form.col []
                    [ Select.custom [Select.onInput UpdateSellAboveCap] (List.map textToItem (capBoolsToString))]
                ]
            , Form.row [displayAbsSellSignalGroup]
                [ Form.colLabel [] [text "Cap"]
                , Form.col []
                    [Input.number [Input.onInput UpdateSellCap]]]
            , Form.row [displayTrendSellSignalGroup]
                [ Form.colLabel [] [text "Timeunit"]
                , Form.col [] <| Radio.radioList "select timeunit" <| List.map (textToRadioButton UpdateSellTimeUnit) timeUnits]
            , Form.row [displayTrendSellSignalGroup]
                [ Form.colLabel [] [text "Time Amount"]
                , Form.col []
                    [ Input.number [Input.onInput UpdateSellTimeAmount]]]
            , Form.row [displayTrendSellSignalGroup]
                [ Form.colLabel [] [text "Percentage"]
                , Form.col []
                    [ Input.number [Input.onInput UpdateSellPercentage]]]
            -- PRIORITY
            , Form.row []
                [ Form.colLabel [] [text "Priority"]
                , Form.col []
                    [Input.number [Input.onInput UpdatePriority]]]
            -- PERCENTAGE
            , Form.row []
                [ Form.colLabel [] [text "Percentage"]
                , Form.col []
                    [Input.number [Input.onInput UpdatePercentage]]]
            ]

strategyModal: Model -> Html.Html Msg
strategyModal model =
    let
        a = ""
    in
        Modal.config DisplayStrategyModal
            |> Modal.large
            |> Modal.h3 [] [text "Create a strategy"]
            |> Modal.body [] [strategyForm model]
            |> Modal.footer []
                [ Button.button [Button.attrs [onClick SubmitStrategy]] [text "Save"]
                , Button.button [Button.attrs [onClick <| DisplayStrategyModal Modal.hiddenState]] [text "Close"]]
            |> Modal.view model.strategyCreation.visible

view : Model -> Html Msg
view model =
    Grid.container [ id "maincontainer"]
        [ CDN.stylesheet
        , Grid.row []
            [ Grid.col [] [createBot model]]
        , Grid.row []
            [Grid.col [] [ strategyModal model]]
        , Grid.row []
            [ Grid.col [] [Button.button [Button.attrs [ onClick <| DisplayStrategyModal Modal.visibleState]] [text "Add another"]]]]