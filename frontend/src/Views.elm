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
import Bootstrap.Alert as Alert
import Bootstrap.ListGroup as ListGroup exposing (Item)
import Bootstrap.Table as Table
import Utils exposing (maybeToString, validToSuccessIndicator, validationToMessage)


dashboardRow: DashboardEntry -> Table.Row msg
dashboardRow entry =
    Table.tr []
        [ Table.td [] [text entry.userName]
        , Table.td [] [text <| toString <| round entry.netWorth]
        ]


emptyDashboard: Table.Row msg
emptyDashboard =
    Table.tr []
        [ Table.td [] [ text "Looks like there is no one playing. Why dont you sign up? Go to 'My bot'"]
        , Table.td [] []
        ]

dashboardBody: Model -> Table.TBody msg
dashboardBody model =
    let
        rows = if List.isEmpty model.dashboard.entries then [emptyDashboard]
            else (List.map dashboardRow model.dashboard.entries)
    in
        Table.tbody [] rows


dashboardGrid: Model -> Html Msg
dashboardGrid model =
    Grid.container []
        [ Grid.row []
            [ Grid.col []
                [ Progress.progress [Progress.value <| floor <| model.dashboard.progress, Progress.label <| String.concat ["Progress: ",toString <|  floor <| model.dashboard.progress, " %"]]
                , br [] []
                , Table.table
                    { options = [Table.striped, Table.small]
                    , thead = Table.simpleThead
                        [ Table.th [] [text "Username"]
                        , Table.th [] [text "Networth"]
                        ]
                    , tbody = dashboardBody model
                    }
                ]
            ]
        ]

strategyRow: UserStrategy ->  Item Msg
strategyRow str =
    ListGroup.li [ListGroup.attrs [ class "justify-content-between" ]]
        [text str.name, (Button.button [Button.small ,Button.attrs[onClick (DeleteStrategyByName str.name), class "float-right"]] [ text "Delete"])]

noStrategies: List(Html Msg)
noStrategies =
    [ Grid.row []
        [ Grid.col []
            [Card.config []
               |> Card.listGroup
                   [ ListGroup.li [ ] [ text "Looks like there are no strategies. Click 'Add strategy'" ]
                   ]
               |> Card.view]
        ]
    ]

someStrategies: Model -> List(Html Msg)
someStrategies model =
    [ Grid.row []
            [ Grid.col []
                [Card.config []
                   |> Card.listGroup (List.map strategyRow model.actualStrates)
                   |> Card.view]
            ]
    ]

strategies: Model -> List (Html Msg)
strategies model =
    let
        rows = if List.isEmpty model.actualStrates then noStrategies
            else someStrategies model
    in
        rows

createBot: Model -> Html Msg
createBot model =
    let
        tooManyStrategies = 4 < (List.length model.actualStrates)
    in
        Grid.container []
            ([ Grid.row []
                [ Grid.col []
                    [ h4 [] [text "Create your bot"]]]
            , Grid.row []
                [ Grid.col []
                    [ Form.form []
                        [Form.row []
                            [ Form.colLabel [] [text "Name your bot"]
                            , Form.col []
                                [ Input.text [Input.onInput UpdateName, Input.value model.userName]
                                , Form.help [] [text "We will display this publicly"]]]
                        , Form.row []
                            [ Form.colLabel [] [text "Your email"]
                            , Form.col []
                                [ Input.email [Input.onInput UpdateEmail, Input.value (maybeToString model.email)]
                                , Form.help [] [text "Used to verify identity in case you win, make sure its correct! Will not be saved or shared."]]
                            ]
                        ]
                    ]
                ]
            , Grid.row []
                [ Grid.col [] [h4 [] [text "Strategies"]]
                ]
            ]
            ++ strategies model ++
            [ Grid.row [] [Grid.col [] [br [] []]]
            , Grid.row []
                [ Grid.col []
                    [ Button.button [Button.attrs [ onClick SubmitStrategies], Button.disabled (model.submitted || List.isEmpty model.actualStrates)] [text "Submit"]
                    , Button.button [Button.attrs [ onClick <| DisplayStrategyModal Modal.visibleState, class "ml-1"], Button.disabled tooManyStrategies] [text "Add strategy"]]
                ]
            ])

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

        triedSave = model.strategyCreation.triedSave

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
            -- SELECTOR
            [ Form.row []
                [ Form.colLabel [] [text "Single company or Sector?"]
                , Form.col [] <| Radio.radioList "selectorTypeRadio" <| List.map (textToRadioButton SelectSelectorType) selectorTypesAsString
                ]
            , Form.row [displaySelectSectorGroup]
                [ Form.colLabel [] [text "Select a sector"]
                , Form.col []
                    [Select.custom [Select.onInput UpdateSelectorValue] (List.map textToItem sectorAsStrings)]
                ]
            , Form.row [displaySelectCompanyGroup]
                [ Form.colLabel [] [text "Select a company"]
                , Form.col []
                    [Select.custom [Select.onInput UpdateSelectorValue] (List.map textToItem (List.map (\c -> c.name) model.companies))]
                ]
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
            , Form.group (validToSuccessIndicator model.strategyCreation.buyCap model.strategyCreation.triedSave)
                [Form.row [displayAbsBuySignalGroup]
                    [ Form.colLabel [] [text "Price"]
                    , Form.col []
                        [ Input.number [Input.onInput UpdateBuyCap]
                        , Form.validationText [] [text <| validationToMessage model.strategyCreation.buyCap triedSave]
                        , Form.help [] [text <| Utils.selectorTypeToInitialPriceText model.strategyCreation.selectorType model.strategyCreation.initialPrice]
                        ]
                    ]
                ]
            , Form.row [displayTrendBuySignalGroup]
                [ Form.colLabel [] [text "Upwards or downwards trend?"]
                , Form.col []
                    [ Select.custom [Select.id "buySignalUp", Select.onInput UpdateBuySignalUp] (List.map textToItem trendStrings)]
                ]
            , Form.group (validToSuccessIndicator model.strategyCreation.buyTimeAmount model.strategyCreation.triedSave)
                [ Form.row [displayTrendBuySignalGroup]
                    [ Form.colLabel [] [text "Days in a row"]
                    , Form.col []
                        [ Input.number [Input.onInput UpdateBuyTimeAmount]
                        , Form.validationText [] [text <| validationToMessage model.strategyCreation.buyTimeAmount triedSave]
                        ]
                    ]
                ]
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
            , Form.group (validToSuccessIndicator model.strategyCreation.sellCap triedSave)
                [ Form.row [displayAbsSellSignalGroup]
                    [ Form.colLabel [] [text "Price"]
                    , Form.col []
                        [ Input.number [Input.onInput UpdateSellCap]
                        , Form.validationText [] [text <| validationToMessage model.strategyCreation.sellCap triedSave]
                        , Form.help []
                            [text <| Utils.selectorTypeToInitialPriceText model.strategyCreation.selectorType model.strategyCreation.initialPrice]
                        ]
                    ]
                ]
            , Form.row [displayTrendSellSignalGroup]
                [ Form.colLabel [] [text "Upwards or downwards trend?"]
                , Form.col []
                    [ Select.custom [Select.id "sellSignalUp", Select.onInput UpdateSellSignalUp] (List.map textToItem trendStrings)]
                ]
            , Form.group (validToSuccessIndicator model.strategyCreation.sellTimeAmount triedSave)
                [ Form.row [displayTrendSellSignalGroup]
                    [ Form.colLabel [] [text "Days in a row"]
                    , Form.col []
                        [ Input.number [Input.onInput UpdateSellTimeAmount]
                        , Form.validationText [] [text <| validationToMessage model.strategyCreation.sellTimeAmount triedSave]]
                    ]
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
            , Card.text [] [ text "A strategy consists of a set of stocks to buy, a trigger for when to buy and a trigger for when to sell."]
            , Card.titleH4 [] [ text "How do I get started?"]
            , Card.text [] [ text "Go to the 'My bot' pane and create your strategies. When you are satisfied hit 'submit'. Be careful; the stock market is an unforgiving place. Once you have submitted your strategy you will not be able to change it!"]
            , Card.titleH4 [] [ text "How do I know if my trading bot is good?"]
            , Card.text [] [ text "Go to the 'Standings' tab to see how your bot stacks up againt other bots"]
            , Card.titleH4 [] [text "The game has already started, am I too late?"]
            , Card.text [] [ text "It doesn't matter when you enter the game. Your bot will magically time travel back into the past and then proceed with trading until it has caught up with the present."]
            , Card.titleH4 [] [ text "When does the game end?"]
            , Card.text [] [text "The game is scheduled to end at approximately 14:30 on thursday. Be sure to sign up before that. If you have won you will be able to claim your prize at the Nordea stand."]

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
                            , strategyModal model
                            ]
                    }
                , Tab.item
                    { link = Tab.link [] [ text "Standings"]
                    , pane =
                        Tab.pane [ class "mt-3" ]
                            [ dashboardGrid model ]
                    }
                ]
            |> Tab.view model.tabstate

errorBox: String -> Html Msg
errorBox message =
    Alert.danger [text message]

successBox: String -> Html Msg
successBox message =
    Alert.success [text message]

notifications: Model -> List (Html Msg)
notifications model =
    List.map (\n -> case n of
        Success s -> successBox s
        Failure s -> errorBox s) model.notifications


view : Model -> Html Msg
view model =
    Grid.container [ id "maincontainer"]
        ( [CDN.stylesheet] ++ (notifications model) ++ [(tabs model)])