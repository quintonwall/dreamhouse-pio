DreamHouse PredictionIO Recommendation Engine
---------------------------------------------

This app uses PredictionIO to provide property recommendations based on users' favorites.

Check out a demo:

[![Demo](http://img.youtube.com/vi/w4060vJhxig/0.jpg)](http://www.youtube.com/watch?v=w4060vJhxig)

Run on Heroku:

1. [Sign up for a free Heroku account](https://heroku.com/signup)
1. [Install the Heroku Toolbelt](https://toolbelt.heroku.com)
1. Deploy the PredictionIO Event Server on Heroku: [![Deploy on Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy?template=https://github.com/jamesward/pio-eventserver-heroku)
1. Create a new app in the PredictionIO Event Server:

        heroku run console app new dreamhouse -a <YOUR EVENT SERVER APP NAME>

1. Deploy the recommendation engine: [![Deploy on Heroku](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/quintonwall/dreamhouse-pio/tree/salesforce-rest)

    Set SALESFORCE_USERNAME and SALESFORCE_PASSWORD env vars to the dreamhouse org where you are favorites are
    
1. Attach your PredictionIO Event Server's Postgres to the recommendation engine app:

    Remove the auto-added Heroku Postgres addon:

        heroku addons:destroy heroku-postgresql -a <YOUR ENGINE APP NAME>

    Lookup the Heroku Postgres Addon ID for the Event Server's Postgres:
    
        heroku addons -a <YOUR EVENT SERVER HEROKU APP NAME>

    Attach the Postgres Addon to the Engine:
    
        heroku addons:attach <YOUR ADDON ID> -a <YOUR ENGINE APP NAME>



Run Locally:

1. Setup a local PredictionIO Event Server: https://github.com/jamesward/pio-eventserver-heroku
1. Setup a local DreamHouse Web App using the `pio` branch: https://github.com/dreamhouseapp/dreamhouse-web-app/tree/pio
1. Setup a local PredictionIO Recommendation Engine: https://github.com/dreamhouseapp/dreamhouse-pio
1. Train the app and run the recommendation engine:

        cd dreamhouse-pio
        source bin/env.sh && DREAMHOUSE_WEB_APP_URL=http://localhost:8200 ACCESS_KEY=<YOUR ACCESS KEY> ./sbt "runMain ServerApp"

1. Check the status of your engine:

    http://localhost:8000

1. Check out the recommendations for an item:

        curl -H "Content-Type: application/json" -d '{"userId": "c1", "numResults": 3 }' -k http://localhost:8000/queries.json
