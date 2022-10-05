# Jira Nag

A CLI application that will nag your developers about their Jira tickets.


## Setup

1. Generate a Personal Access Token in JIRA (https://issues.redhat.com/secure/ViewProfile.jspa?selectedTab=com.atlassian.pats.pats-plugin:jira-user-personal-access-tokens)
2. Create an `.env` file in the root of this project with the following variables:
```dotenv
APP_JIRA_TOKEN=your-jira-token
```


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw clean compile quarkus:dev
```


## Creating a new e-mail

To create a new e-mail:

1. Create the checked template in `src/main/resources/templates/emails`
2. Add the template to the `com.github.gastaldi.jiranag.JiraNagConfig.Email.Template` enum
3. Add the enum to the `switch` block in the `com.github.gastaldi.jiranag.JiraNag#sendEmail` method 