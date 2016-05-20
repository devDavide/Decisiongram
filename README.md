# Decisiongram messenger for Android
Decisiongram is an unofficial Telegram client for Android, that integrate inside group chat a new useful features that allow members to easily take shared decision about whatever they want, using a poll based system (similar to [**Doodle**](https://doodle.com/)).

This was also the final project of my bachelor in computer science, you can find the complete final paper [right here](https://drive.google.com/file/d/0BxMIqTyCdaCnSFh3RFc3ekZFZ2M/view?usp=sharing)

### Project description

The project aims to build an instant messaging mobile application for Android, that help user groups at taking shared decisions, providing an in-app fully integrated support to this process. In a nutshell the system that I buildt is based on a voting mechanism, where each user can make a set of proposals that will be voted by others members.
When a member of a group, wants the group to make a shared decision about something, that user will create a new “decision” that consists in a poll with different of options. The decision and each one of the options have an title and a long description where is possible to insert additional information. After a decision has been created, members can vote for the options, choosing for each one “yes”, or “no”.

Options will be shown ordered by positive vote count, in this way users will first see the ones that have the highest likelihood of being chosen, so they will tend to vote for those; making it easier to reach an agreement. In each moment it will be also possible to have a clear view of which users voted, which did not, reminding them to cast their vote. Furthermore once a decision has been created, it will be no longer necessary to read thousand of messages, for having an idea about the opinion expressed so far, that data will be presented in a specific screen right inside the app.

### Why creating new app, and not just using Doodle ?

It might be said that is also possible to perform tasks similar to the ones performed by Decisiongram, by sending in a Telegram group chat the link to a Doodle, or to a shared Google spreadsheet. Nevertheless I consider that by using Decisiongram, according to the way it is implemented, the task of making a decision could be performed in a much more easy way and with a better support, for the following reasons.

1. Notifications about decisions, will be reported, within special messages, just inside the chat thread, making so those transactions, part of the communication flow between group members. 
2. Decisiongram being based on Telegram, is primarily a messaging app, that beside providing support for managing decisions and voting options, it even provides a natural support for discussing about such topics, helping in this way users in finding consensus.
3. All the decisions created within a group will automatically inherit the members from the group. In this way it will be not necessary to manually re-add all members to each new decision.
4. As the poll will be created right inside a preexisting group of users (the group-chat) it will be  easy to see who voted and who did not; making it easy to remind group members to vote, if they have not done it yet.
5. If the decision space (number of members and number of options) is large it could be pretty hard, using a software like Doodle, to figure out what are the options that actually have the highest likelihood of being chosen, as for doing so, it will be necessary to scroll through all the decision space. Decisiongram shows you the options ordered by positive vote count, in this way it would be easy to have a clear vision of the options that have highest likelihood of being chosen. This way the last users that vote could be somehow influenced, by the ones that voted before; this conditioning could be helpful in order to reach the widest agreement on the populars options. In any case lesser-voted options are not hidden they are simply moved down in the list. 
6. It is possible to describe in depth a decision or an option by adding a description. Such description can even contains URLs.
7. Having these features fully integrated in the same app, will allow the user to easily switch between the chat and the poll, removing so the friction of opening another app or the web-browser, that may even required to sign in or to sign up in order to access the poll.

### App deployment 

For Android app the natural way for doing so is publishing it on the Google Play Store. I tried to do so, but unfortunately I am having some issues, as Google suspended my app asking to send them some proof that Telegram authorizes me to publishing Decisiongram. I wrote to the Telegram support team, but at the moment I am still waiting for an answer.
