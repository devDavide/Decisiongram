# Decisiongram messenger for Android
Decisiongram is an unofficial Telegram client for Android, that integrate inside group chat a new useful features that allow members to easily take shared decision about whatever they want, using a poll based system (similar to [**Doodle**](https://doodle.com/)).

This was also the final project of my bachelor in computer science, you can find the complete final paper [right here](https://drive.google.com/file/d/0BxMIqTyCdaCnSFh3RFc3ekZFZ2M/view?usp=sharing)

### Project description

The project is about to build a messaging system that help users groups in taking decision, providing an in-app fully integrated support to this process. Basically the system that i will build is based on democratic vote, where each user can make proposal that will be voted by others, as vote system seems to works kind of good for finding consensus in a democratic systems,since the 6th century BC.

So if a member of a group, wants the group to take a shared decision about something, that user will create a new “decision” that consist in a poll with different of options. The decision and each one of its options have univocal a title and a long description, where is possible to insert additional information about. After a decision has been created, members can vote the for the options saying for each one “yes”, or “no”.

Option will be showed ordered by positive vote count, in this way users will saw at first the one that have the highest likelihood of being chosen, so they will tend to vote for those; making easier to reach an agreement on one single option. Furthermore in each moment will be possible to have a clear idea of the users that votes, and the one that don't, reminding so to those last to do so.

### Why creating new app, and not just using Doodle ?

It is true that could be also possible to achieve task similar to the ones performed by Decisiongram, by sending in a Telegram group chat something the link to a Doodle, or to a shared Google spreadsheet. But with using Decisiongram, according to the way it is implemented, the task of making a decision will be performed in a much more easy way and with a better support, for the following reasons.

1. Notifications about decisions, will be reported, within special messages, just inside the chat thread, making so those transactions, part of the communication flow between group members. 
2. Decisiongram being based on Telegram, is primarily a messaging app, that beside providing support for managing decisions and voting options, it even provides a natural support for discussing about such topics, helping in this way users in finding consensus.
3. All the decisions created within a group will automatically inherit the members from the group. In this way it will be not necessary to manually re-add all members to each new decision.
4. As the poll will be created right inside a preexisting group of users (the group-chat) it will be  easy to see who voted and who did not; making it easy to remind group members to vote, if they have not done it yet.
5. If the decision space (number of members and number of options) is large it could be pretty hard, using a software like Doodle, to figure out what are the options that actually have the highest likelihood of being chosen, as for doing so, it will be necessary to scroll through all the decision space. Decisiongram shows you the options ordered by positive vote count, in this way it would be easy to have a clear vision of the options that have highest likelihood of being chosen. This way the last users that vote could be somehow influenced, by the ones that voted before; this conditioning could be helpful in order to reach the widest agreement on the populars options. In any case lesser-voted options are not hidden they are simply moved down in the list. 
6. It is possible to describe in depth a decision or an option by adding a description. Such description can even contains URLs.
7. Having these features fully integrated in the same app, will allow the user to easily switch between the chat and the poll, removing so the friction of opening another app or the web-browser, that may even required to sign in or to sign up in order to access the poll.

### App deployment 

For Android app the natural way for doing so is publishing it on the Google Play Store. I tried to do so, but unfortunately I am having some issues, as Google suspended my app because it violates the impersonation and intellectual property provision. So they asked me to send them some proof that Telegram authorizes me to publishing Decisiongram. I wrote to the Telegram support team, but at the moment I am still waiting for an answer.
