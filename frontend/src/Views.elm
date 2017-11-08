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
import Bootstrap.Card as Card
import Bootstrap.Tab as Tab
import Bootstrap.Progress as Progress
import Utils


dashboardRow: DashboardEntry -> Html Msg
dashboardRow entry =
    Grid.row []
        [ Grid.col [] [text entry.userName]
        , Grid.col [] [text (toString entry.netWorth)]]

dashbardEmptyMessage: Html Msg
dashbardEmptyMessage= Grid.row [] [Grid.col [] [text "Looks like there is no one play. Why dont you sign up? Go to 'My bot'"]]

dashboardUsers: Model -> List (Html Msg)
dashboardUsers model =
    let
        rows = if List.isEmpty model.dashboard.entries then [dashbardEmptyMessage]
            else (List.map dashboardRow model.dashboard.entries)
    in
        rows


dashboardGrid: Model -> Html Msg
dashboardGrid model =
    Grid.container []
        ([Grid.row []
            [ Grid.col []
                [Progress.progress [Progress.value <| floor model.dashboard.progress, Progress.label "The games progress"]]]]
        ++ dashboardUsers model)

strategyRow: UserStrategy -> Html msg
strategyRow str =
    Grid.row []
        [ Grid.col [] [ text str.name]
        , Grid.col []
            [ Button.button [] [ text "Details"]]
        , Grid.col []
            [ Button.button [] [ text "Edit"]]]

noStrategies: List(Html Msg)
noStrategies =
    [ Grid.row []
        [ Grid.col [] [text "Looks like you dont have any strategies. Press 'Add another' to add one"]]]

strategies: Model -> List (Html Msg)
strategies model =
    let
        rows = if List.isEmpty model.actualStrates then noStrategies
            else (List.map strategyRow model.actualStrates)
    in
        rows

createBot: Model -> Html Msg
createBot model =
    Grid.container []
        ([ Grid.row []
            [ Grid.col [] [text "Create Bot"]]
        , Grid.row []
            [Grid.col []
                [Input.text [Input.onInput UpdateName]]
            ]
        , Grid.row []
            [ Grid.col [] [text "Strategies"]]]
        ++ strategies model ++
        [ Grid.row []
            [ Grid.col [] [Button.button [Button.attrs [onClick SubmitStrategies]] [text "Submit"]]]])

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
                [ Form.colLabel [] [text "Name your strategy"]
                , Form.col []
                    [ Input.text [Input.onInput UpdateStrategyName, Input.attrs [ value (Utils.maybeToString model.strategyCreation.name)] ]]]
            -- SELECTOR
            , Form.row []
                [ Form.colLabel [] [text "Single company or Sector?"]
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
                [ Form.colLabel [] [text "When to buy the stocks?"]
                , Form.col []
                    [ Select.custom [Select.id "buySignalType",Select.onInput SelectBuySignalType] (List.map textToItem (signalTypesAsString))]
                ]
            , Form.row [displayAbsBuySignalGroup]
                [ Form.colLabel [] [text "Buy above or below price?"]
                , Form.col []
                    [ Select.custom [Select.onInput UpdateBuyAboveCap] (List.map textToItem (capBoolsToString))]
                ]
            , Form.row [displayAbsBuySignalGroup]
                [ Form.colLabel [] [text "Price"]
                , Form.col []
                    [Input.number [Input.onInput UpdateBuyCap]]]
            , Form.row [displayTrendBuySignalGroup]
                [ Form.colLabel [] [text "Upwards or downwards trend?"]
                , Form.col []
                    [ Select.custom [Select.id "buySignalUp", Select.onInput UpdateBuySignalUp] (List.map textToItem trendStrings)]]
            , Form.row [displayTrendBuySignalGroup]
                [ Form.colLabel [] [text "Days in a row"]
                , Form.col []
                    [ Input.number [Input.onInput UpdateBuyTimeAmount]]]
            -- SELL SIGNAL
            , Form.row []
                [ Form.colLabel [] [text "When to sell the stock?"]
                , Form.col []
                    [ Select.custom [Select.id "sellSignalType",Select.onInput SelectSellSignalType] (List.map textToItem (signalTypesAsString))]
                ]
            , Form.row [displayAbsSellSignalGroup]
                [ Form.colLabel [] [text "Sell above or below price?"]
                , Form.col []
                    [ Select.custom [Select.onInput UpdateSellAboveCap] (List.map textToItem (capBoolsToString))]
                ]
            , Form.row [displayAbsSellSignalGroup]
                [ Form.colLabel [] [text "Price"]
                , Form.col []
                    [Input.number [Input.onInput UpdateSellCap]]]
            , Form.row [displayTrendSellSignalGroup]
                [ Form.colLabel [] [text "Upwards or downwards trend?"]
                , Form.col []
                    [ Select.custom [Select.id "sellSignalUp", Select.onInput UpdateSellSignalUp] (List.map textToItem trendStrings)]
                ]
            , Form.row [displayTrendSellSignalGroup]
                [ Form.colLabel [] [text "Days in a row"]
                , Form.col []
                    [ Input.number [Input.onInput UpdateSellTimeAmount]]
                ]
            ]

strategyModal: Model -> Html.Html Msg
strategyModal model =
    Modal.config DisplayStrategyModal
        |> Modal.large
        |> Modal.h1 [] [text "Create a strategy"]
        |> Modal.body [] [strategyForm model]
        |> Modal.footer []
            [ Button.button [Button.attrs [onClick SaveStrategy]] [text "Save"]
            , Button.button [Button.attrs [onClick <| DisplayStrategyModal Modal.hiddenState]] [text "Close"]]
        |> Modal.view model.strategyCreation.visible

tutorial: Model -> Html Msg
tutorial model =
    Card.config [ Card.attrs [ ] ]
        |> Card.header [ class "text-center" ]
            [ h3 [ class "mt-2" ] [ text "Tutorial" ]
            ]
        |> Card.block []
            [ Card.titleH4 [] [ text "The aim of the game" ]
            , Card.text [] [ text "The aim of the game is to make a killing in the stock market using at most 5 simple strategies." ]
            , Card.titleH4 [] [ text "A strategy?"]
            , Card.text [] [ text "A strategy consists of a set of stocks to buy (stock selector), a trigger for when to buy (buy trigger) and a trigger for when to sell (sell trigger)."]
            , Card.titleH4 [] [ text "How do i get started?"]
            , Card.text [] [ text "Go to the 'My bot' pane and create your strategies. When you are satisfied hit 'submit'. Be careful; the stock market is an unforgiving place. Once you have submitted your strategy you will not be able to change it!"]
            , Card.titleH4 [] [ text "How do i know if my trading bot is good?"]
            , Card.text [] [ text "Go to the 'dashboard' pane to see how your bot stack up againt other bots"]
            , Card.titleH4 [] [text "I am late to the game, should i just give up?"]
            , Card.text [] [ text "It doesn't matter when you enter the game. Your bot will magically time travel back into the past and then proceed with trading until it has caught up with the present."]
            ]
        |> Card.view

tabs: Model -> Html Msg
tabs model =
    Tab.config TabMsg
            |> Tab.items
                [ Tab.item
                    { link = Tab.link [] [ text "Tutorial" ]
                    , pane =
                        Tab.pane [ class "mt-3" ]
                            [ tutorial model]
                    }
                , Tab.item
                    { link = Tab.link [] [ text "My bot" ]
                    , pane =
                        Tab.pane [ class "mt-3" ]
                            [ createBot model
                            , Button.button [Button.attrs [ onClick <| DisplayStrategyModal Modal.visibleState]] [text "Add another"]
                            , strategyModal model
                            ]
                    }
                , Tab.item
                    { link = Tab.link [] [ text "Dashboard"]
                    , pane =
                        Tab.pane [ class "mt-3" ]
                            [ dashboardGrid model ]
                    }
                ]
            |> Tab.view model.tabstate


view : Model -> Html Msg
view model =
    Grid.container [ id "maincontainer"]
        [ CDN.stylesheet
        , tabs model]