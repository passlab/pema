# OpenMP Load Balance Analysis using Eclipse Tracecompass

## Set up Tracing Project
If haven't already done so set up the Tracing Project before moving on.
Follow these steps until you reach Plug-in Project: https://github.com/passlab/pema/blob/main/experiment/tracecompass/getStarted/README.md

## Cloning the Repository
#### Open Terminal and follow these commands:

git clone https://github.com/passlab/pema.git

cd pema

## Importing the Project into Eclipse
1. Open Eclipse
2. Click File > Open Projects from the File system.
3. In the dialog box, click directory... and navigate to the pema folder
4. Keep opening folders until you locate the lbanalysis folder
5. Select it and click Finish
6. The project should now appear in the Project Explorer on the left-hand side.
![Screenshot 2025-02-14 at 10 23 41 AM](https://github.com/user-attachments/assets/60dde19d-4435-41eb-9b87-c4839f8b8b15)

## Running the Sample View in Eclipse
1. In the lb analysis project, expand the src folder
2. Keep opening the folders until you find the SampleView file
3. Right-click on SampleView
4. Click Run As > Eclipse Application
5. The view should be visible 


Keep in mind that the view will only be visible if the tracing data is running!
