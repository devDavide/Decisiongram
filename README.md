# Decisiongram messenger for Android
Decisiongram is an unofficial Telegram client for Android, that integrate inside group chat a new useful features that allow members to easily take shared decision about whatever they want, using a poll based system (similar to [**Doodle**](https://doodle.com/)).

### Project description

The project is about to build a messaging system that help users groups in taking decision, providing an in-app fully integrated support to this process. Basically the system that i will build is based on democratic vote, where each user can make proposal that will be voted by others, as vote system seems to works kind of good for finding consensus in a democratic systems,since the 6th century BC.

So if a member of a group, wants the group to take a shared decision about something, that user will create a new “decision” that consist in a poll with different of options. The decision and each one of its options have univocal a title and a long description, where is possible to insert additional information about. After a decision has been created, members can vote the for the options saying for each one “yes”, or “no”.

Option will be showed ordered by positive vote count, in this way users will saw at first the one that have the highest likelihood of being chosen, so they will tend to vote for those; making easier to reach an agreement on one single option. Furthermore in each moment will be possible to have a clear idea of the users that votes, and the one that don't, reminding so to those last to do so.

### Why creating new app, and not just using Doodle ?

It is true that could be also possible to achieve task similar to the ones performed by Decisiongram, by sending in a Telegram group chat something the link to a Doodle, or to a shared Google spreadsheet. But with using Decisiongram, according to the way it is implemented, the task of making a decision will be performed in a much more easy way and with a better support, for the following reasons.

1. Notification about decisions, will be reported, with special messages, just inside the chat thread, making so those transaction, part of the communication between group members.
2. Decisiongram as it is based on Telegram, is primarily a Messaging app, so moreover than providing support for voting options, it even provide a natural support for discussing about that, helping so in finding consensus.
3. If different decision are created in the same group, it not necessary to add each time all the members, cause decision's members are exactly the same as the group members.
4. As the poll will be created right inside a preexisting group of users (the group-chat) it will be easy to see who answered and who didn't; making so easy to remind group's members to vote, if they didn’t.
5. If the decision space (number of members and number of options) is large could be pretty hard, using software like Doodle, figure out what are the options that actually have the highest likelihood of being chosen, as for doing so, it will be necessary to scroll through all the decision space. Decisiongram shows you the options ordered by positive vote count, in this way it’s easy to have a clear vision of the options that have highest likelihood of being chosen. So the last users that vote could be somehow influenced, by the one that voted before; this conditioning could be helpful in order to reach a widest agreement, on the populars options. Anyway options with less votes are not hidden they are just moved down in the list.
6. It is possible to describe deeply a decision or an option by adding a description, that can even contains URLs.
7. Having everything integrated in the same app, will make for the user easier to switch between the chat and the poll; removing so the friction of opening another app or the web-browser, that may even required to sign in or to sign up in order to access the poll.
