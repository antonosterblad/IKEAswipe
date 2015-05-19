package agilec.ikeaswipe.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.support.v4.app.Fragment;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.json.JSONException;

import agilec.ikeaswipe.models.AllSteps;
import agilec.ikeaswipe.activities.ArFindActivity;
import agilec.ikeaswipe.activities.ArStepsActivity;
import agilec.ikeaswipe.utils.OnSwipeTouchListener;
import agilec.ikeaswipe.R;
import agilec.ikeaswipe.models.Step;
import agilec.ikeaswipe.activities.SwipeActivity;

/**
 * Functionality and layout for Step by step
 */
public class StepByStepFragment extends Fragment {

  private AllSteps stepHandler;         // Handles all steps
  private ImageButton completedStepBtn;
  private ImageButton helpBtn;
  private TextView header;
  private ImageView imgView;
  private Button checkbarButton;

  //The "x" and "y" position of the "Show Button" on screen.
  private Point p;

  /**
   * int stepNumber is used to track the current step and update the corresponding variable
   * currentStep in the parent SwipeActivity.
   * int prevStep is used to track the previous step.
   * boolean prevIsCompleted is used to see if the step is completed or not.
   */
  private int stepNumber;
  private int prevStep;
  private boolean prevIsCompleted;

  private ImageSwitcher imageSwitcher;

  /**
   * transitionToNextImage makes the currently displayed image slide upward and a new, specified
   * image slide in from below.
   * @param id
   * @author @martingrad
   */
  public void transitionToNextImage(int id){
    Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_up);
    Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_up);
    imageSwitcher.setInAnimation(in);
    imageSwitcher.setOutAnimation(out);
    imageSwitcher.setImageResource(id);
  }

  /**
   * transitionToPreviousImage makes the currently displayed image slide downward and a new,
   * specified image slide in from above.
   * @author @martingrad
   * @param id
   */
  public void transitionToPreviousImage(int id){
    Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_down);
    Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_down);
    imageSwitcher.setInAnimation(out);
    imageSwitcher.setOutAnimation(in);
    imageSwitcher.setImageResource(id);
  }

  /**
   * setImage changes the image source of imgView depending on the current step
   *
   * @param stepNumber
   * @author @emmaforsling @martingrad @byggprojektledarn
   */
  private void setImage(int stepNumber) {
    String imgUrl = stepHandler.getSteps().get(stepNumber).getImgUrl(); // Get the image url for the instruction image
    int id = getResources().getIdentifier(imgUrl, "drawable", getActivity().getPackageName()); // Get the id

    if(stepNumber > prevStep) {
      transitionToNextImage(id);
    } else {
      transitionToPreviousImage(id);
    }
  }

  private void setHeader(int stepNumber) {
    String title = stepHandler.getSteps().get(stepNumber).getTitle(); // Get the image url for the instruction image
    header.setText(title); // Set the correct image using id
  }

  /**
   * Check if step is completed or not
   */
  private void loadIsCompletedButton(boolean isCompleted, View view, int stepNumber) {
    if (isCompleted == false) {
      ((ImageButton) view.findViewById(R.id.completedStepButton)).setImageResource(R.drawable.ic_action_done_before);
      //To get the right button
      checkBarButtonView(stepNumber, view);
      //Change color of the button
      checkbarButton.setBackgroundColor(getResources().getColor(R.color.grey));
    } else {
      ((ImageButton) view.findViewById(R.id.completedStepButton)).setImageResource(R.drawable.ic_action_done_after);
      //To get the right button
      checkBarButtonView(stepNumber, view);
      //Change color of the button
      checkbarButton.setBackgroundColor(getResources().getColor(R.color.green));
    }
  }

  /**
   * Sets the background color for the checkbar-buttons that are not the current one to a color with opacity
   *
   * @param view
   * @param prevIsCompleted check if previous step is checked
   * @param prevStep        number for the previous step
   * @author LinneaMalcherek
   */
  private void setDefaultColorButtons(View view, boolean prevIsCompleted, int prevStep) {
    //Get view
    checkBarButtonView(prevStep, view);

    if (prevIsCompleted == true) {
      checkbarButton.setBackgroundColor(getResources().getColor(R.color.greenOpacity));
    } else {
      checkbarButton.setBackgroundColor(getResources().getColor(R.color.greyOpacity));
    }
  }

  /**
   * Sets checkbarButton to the one for the current step
   *
   * @param stepNumber Number for the current step
   * @param view
   * @author LinneaMalcherek
   */
  private void checkBarButtonView(int stepNumber, View view) {
    String checkbarButtonUrl = stepHandler.getSteps().get(stepNumber).getCheckbarButtonUrl();  // Get the checkbar button url for the instruction image
    int id = getResources().getIdentifier(checkbarButtonUrl, "id", getActivity().getPackageName()); // Get the id from res
    checkbarButton = (Button) view.findViewById(id); // Set the correct Button using id
  }

  /**
   * This function is overridden to save the current step number when the activity is recreated
   *
   * @param outState
   */
  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putInt("stepNumber", stepNumber);
    super.onSaveInstanceState(outState);
  }

  /**
   * OnCreateView initializes the instance variables of the fragment.
   * @param inflater
   * @param container
   * @param savedInstanceState
   * @author @martingrad
   * @return
   */
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    final View view = inflater.inflate(R.layout.fragment_step_by_step, container, false); // Inflate the layout for this fragment
    header = (TextView) view.findViewById(R.id.stepByStepHeader); // Define header id connection

    // Initialize the imageSwitcher
    imageSwitcher = (ImageSwitcher) view.findViewById(R.id.imageSwitcher1);
    imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
      public View makeView() {
        ImageView myView = new ImageView(getActivity().getApplicationContext());
        myView.setMinimumHeight(3000);    // Ugly... To keep the image vertically centered.
        return myView;
      }
    });

    try {
      stepHandler = new AllSteps("kritter_steps.json", getActivity());
    } catch (JSONException e) {
      e.printStackTrace();
    }

    // If ArFindAllActivity was the last active Activity
    Intent intent = getActivity().getIntent();
    String completeModelUrl = intent.getStringExtra("completeModel"); // Get the completeModelUrl
    stepNumber = intent.getIntExtra("currentStep", 0);           // Get the current step
    if (completeModelUrl != null) { // If the step was correct and found in the AR Activity
      // Mark the step as done

      // Load a different button depending on if the step is completed or not
      loadIsCompletedButton(true, view, stepNumber);

      // Mark the step as done or undone
      ((SwipeActivity) getActivity()).setCompletedStep(stepNumber, true);
    }

    // Set listener to the view
    view.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
      // If user swipe down -> up
      public void onSwipeTop() {
        if (stepNumber != 6) {
          // Increment the stepNumber
          stepNumber++;

          //Number for previous step
          prevStep = stepNumber - 1;

          //To see if the step is completed
          prevIsCompleted = ((SwipeActivity) getActivity()).getCompletedStep(stepNumber - 1);

          //Set opacity of background color for previous button
          setDefaultColorButtons(view, prevIsCompleted, prevStep);

          // Change the image source
          setImage(stepNumber);

          // Change header
          setHeader(stepNumber);

          // Load the step completed button
          loadIsCompletedButton(((SwipeActivity) getActivity()).getCompletedStep(stepNumber), view, stepNumber);

          // Call the setStepNumber function in SwipeActivity to change the current step number
          try {
            ((SwipeActivity) getActivity()).setStepNumber(stepNumber);
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      }

      // If user swipe up -> down
      public void onSwipeBottom() {
        if (stepNumber != 0) {
          // Decrement the stepNumber
          stepNumber--;

          //Number for previous step
          prevStep = stepNumber + 1;

          //To see if the step is completed
          prevIsCompleted = ((SwipeActivity) getActivity()).getCompletedStep(stepNumber + 1);

          //Set opacity of background color for previous button
          setDefaultColorButtons(view, prevIsCompleted, prevStep);

          // Change the image source
          setImage(stepNumber);

          // Change header
          setHeader(stepNumber);

          // Load the step completed button
          loadIsCompletedButton(((SwipeActivity) getActivity()).getCompletedStep(stepNumber), view, stepNumber);

          // Call the setStepNumber function in SwipeActivity to change the current step number
          try {
            ((SwipeActivity) getActivity()).setStepNumber(stepNumber);
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      }
    });

    // Find the ImageView from the .xml
    imgView = (ImageView) view.findViewById(R.id.steps);

    // Extract the id for the current step

    // Check if any step has been stored, if so - remain on the last step stored.
    if (savedInstanceState != null) {
      stepNumber = savedInstanceState.getInt("stepNumber");
    } else {
      stepNumber = getArguments().getInt("stepNumber");
    }

    // Set the image source
    setImage(stepNumber);
    setHeader(stepNumber);

    completedStepBtn = (ImageButton) view.findViewById(R.id.completedStepButton);

    completedStepBtn.setOnClickListener(new View.OnClickListener() {
      /**
       * onClick function for the completedStepBtn
       * @author @emmaforsling @marcusnygren
       * @param v The view
       */
      @Override
      public void onClick(View v) {
        // Check if the current stepNumber is completed
        boolean isCompleted = ((SwipeActivity) getActivity()).getCompletedStep(stepNumber);

        // When the button is clicked, the status should be reversed
        isCompleted = !isCompleted;

        // Load a different button depending on if the step is completed or not
        loadIsCompletedButton(isCompleted, view, stepNumber);

        // Mark the step as done or undone
        ((SwipeActivity) getActivity()).setCompletedStep(stepNumber, isCompleted);
      }
    });

    helpBtn = (ImageButton) view.findViewById(R.id.stepByStepHelpButton);

    helpBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showPopup(getActivity(), p);
      }
    });

    return view;
  }

  /**
   * Get the x and y position after the button is draw on screen
   * (It's important to note that we can't get the position in the onCreate(),
   * because at that stage most probably the view isn't drawn yet, so it will return (0, 0))
   *
   * The function will be run from SwipeActivity when onWindowFocusChanged return true
   * @author @antonosterblad
   */
  public void findPos() {

    int[] location = new int[2];
    helpBtn = (ImageButton) getActivity().findViewById(R.id.stepByStepHelpButton);

    // Get the x, y location and store it in the location[] array
    // location[0] = x, location[1] = y.
    helpBtn.getLocationOnScreen(location);

    //Initialize the Point with x, and y positions
    p = new Point();
    p.x = location[0];
    p.y = location[1];

  }

  /**
   * Displays pop up and it's contents
   * @param context Context
   * @param p Point for help buttons position
   *
   * @author @antonosterblad @ingelhag @emmaforsling
   */
  // The method that displays the popup.
  private void showPopup(final Activity context, Point p) {
    findPos();
    int popupWidth = 600;
    int popupHeight = 400;


    // Inflate the popup_layout.xml
    LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
    LayoutInflater layoutInflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View layout = layoutInflater.inflate(R.layout.help_popup_layout, viewGroup);

    // Creating the PopupWindow
    final PopupWindow popup = new PopupWindow(context);
    popup.setContentView(layout);
    popup.setWidth(popupWidth);
    popup.setHeight(popupHeight);
    popup.setFocusable(true);

    // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
    int OFFSET_X = -435;
    int OFFSET_Y = 75;

    // Clear the default translucent background
    popup.setBackgroundDrawable(new BitmapDrawable());

    // Displaying the popup at the specified location, + offsets.
    popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);

    // Getting a reference to ARHelp button, and send to new activity when clicked
    ImageButton ARHelp = (ImageButton) layout.findViewById(R.id.ARHelp);
    ARHelp.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent arIntent = new Intent(getActivity(), ArStepsActivity.class);
        arIntent.putExtra("currentTab",1);
        startActivity(arIntent);

      }
    });

    // Displaying the popup at the specified location, + offsets.
    popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);

    // Getting a reference to ARHelp button, and send to new activity when clicked
    ImageButton ARCheckComplete = (ImageButton) layout.findViewById(R.id.ARCheckComplete);
    ARCheckComplete.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent arIntent = new Intent(getActivity(), ArFindActivity.class);
        Step currentStep = stepHandler.getSteps().get(stepNumber);

        arIntent.putExtra("article", currentStep.getCompleteModelUrl());
        arIntent.putExtra("currentTab", 1);
        arIntent.putExtra("currentStep", currentStep.getStep());
        startActivity(arIntent);
      }
    });

  }
}