# UCLA Class Status Checker in Docker/balena

## Introduction

This program runs on docker through balenaOS and checks the UCLA Registrar for updates in user-specified classes.

## Installation & Usage Guide

### balena Setup
(Skip if you already have balenaOS working)
1. If you haven't got a [balena](https://balena.io) account, visit [dashboard.balena-cloud.com](https://dashboard.balena-cloud.com/signup) and sign up.
1. Start a new applicaton on [balena](https://balena.io) download the .zip file, format the SD card in FAT32 and extract it to your SD card.
1. Insert the SD card into the device, power it up using the micro-usb cable and connect to the Internet.
1. After about 10 minutes your new device should show up on your application dashboard.

### Deployment

Clone this repo to your local disk

`$ git clone https://github.com/ViciousCupcake/balena-ucla-class-status-checker.git`

Install the [balena CLI](https://github.com/balena-io/balena-cli/blob/master/INSTALL.md)

Next, download the [Push Me App](https://apps.apple.com/us/app/push-me-stay-in-the-loop/id1208277751), create a API key, and insert it into line 15, surrounded by double-quotes.

e.g. If my key was `123abc456def`, line 15 would read

`private static final String PUSH_NOTIFICATION_IDENTIFIER = "123abc456def";`

Next, specify the interval of how often the application should check for new updates on line 16. Do this by editing the `UPDATE_INTERVAL_MS` field. Note that this number is measured in milliseconds, so 15 minutes would be `15 * 60 * 1000`.

I recommend keeping it at 15 minutes, since too often means excessive data usage which may anger your internet provider.

If you keep it at 15 min, line 16 should read

`public static final long UPDATE_INTERVAL_MS = 15 * 60 * 1000;`

Finally, add the URLs of the classes you want to check. To find these URLs, go [here](https://sa.ucla.edu/ro/public/soc), search for your class, click the drop down on the left to expose discussions, and click the specific discussion that you want the program to check. A new window should open. Copy paste the url [(example)](https://sa.ucla.edu/ro/Public/SOC/Results/ClassDetail?term_cd=20F&subj_area_cd=MATH%20%20%20&crs_catlg_no=0032A%20%20%20&class_id=262206201&class_no=%20001%20%20), and paste it onto line 17, surrounded by double quotes. For each additional entry, do the same thing, but divide entries with a comma. Don't forget double-quotes!

**You're almost done!**

Next, login into the balena CLI

`$ balena login`

Then push this cloned repo onto your balena application:

`$ balena push [APPLICATIONNAME]`

And enjoy!
