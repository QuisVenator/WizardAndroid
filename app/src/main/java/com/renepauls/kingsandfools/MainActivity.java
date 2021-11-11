package com.renepauls.kingsandfools;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.ViewTreeObserver;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MyApp app;
    private static double cardAspectRatio = 208.0/303.0;
    private float screenWidth;
    private float screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = (MyApp) getApplication();

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        Deck.initialize(getApplicationContext());
        app.gameLogic.mainActivity = this;
    }

    public void addCards(List<Card> cards) {
        LinearLayout cardsParent = findViewById(R.id.cardsParentRow1);
        if(cardsParent.getChildCount() > 0) {
            Log.d("GiveCards", "Tried to give cards to non-empty hand...clearing");
        }
        clearCards(null);

        LinearLayout cardsParent2 = findViewById(R.id.cardsParentRow2);
        int previousId = -1;
        int cardWidth = (int) (screenWidth / 4.5);
        int cardHeight = (int) (screenWidth / 4.5 / cardAspectRatio);
        int minLeftMargin = cardWidth / 2;
        int noScrollMargin = (int) ((screenWidth - cardWidth) / (cards.size() - 1));
        int leftMargin = Integer.max(minLeftMargin, noScrollMargin);

        Handler handler = new Handler();

        for(int i = 0; i < cards.size(); i++) {
            ImageView card = new ImageView(this);
            card.setTag(cards.get(i).getResourceName());
            card.setOnClickListener(this::onCardSelected);
            card.setImageResource(cards.get(i).getResId());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, cardHeight);
            card.setId(View.generateViewId());
            previousId = card.getId();
            if(i != 0 && i != 10)
                params.leftMargin = leftMargin - cardWidth;
            card.setClickable(true);
            card.setPadding(3, 3, 3, 3);
            card.setLayoutParams(params);

            if(i < 10)
                handler.postDelayed(() -> cardsParent.addView(card), 200*i + 1000);
            else
                handler.postDelayed(() -> cardsParent2.addView(card), 200*i + 1000);
        }
    }

    private void clearCards(View view) {
        ViewGroup[] rows = {
                findViewById(R.id.cardsParentRow1),
                findViewById(R.id.cardsParentRow2),
                findViewById(R.id.playedCardsRow)
        };
        Handler handler = new Handler();

        int counter = 0;
        for(ViewGroup row : rows) {
            int cardsInRow = row.getChildCount();
            for(int i = cardsInRow-1; i >= 0; i--) {
                View card = row.getChildAt(i);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        row.removeView(card);
                    }
                }, (cardsInRow-i+counter)*100);
            }
            counter += row.getChildCount();
        }
        ((ViewGroup)findViewById(R.id.mainLayout)).getOverlay().clear();
    }

    public void onCardSelected(View card) {
        ViewGroup parent = (ViewGroup)card.getParent();

        float originalX = getAbsX(card);
        float originalY = getAbsY(card);

        View firstCard = parent.getChildAt(0);
        if(card == firstCard && parent.getChildCount() > 1) {
            View newFirstCard = parent.getChildAt(1);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) newFirstCard.getLayoutParams();
            params.leftMargin = 0;
            newFirstCard.setLayoutParams(params);
        }

        parent = (ViewGroup) card.getParent();
        parent.setLayoutTransition(null);
        parent.removeView(card);
        parent.setLayoutTransition(new LayoutTransition());
        ViewGroup playedCardsRow = findViewById(R.id.playedCardsRow);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(card.getWidth(), card.getHeight());
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        card.setLayoutParams(params);
        playedCardsRow.addView(card);
        ViewTreeObserver observer = playedCardsRow.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);

                float newX = getAbsX(card);
                float newY = getAbsY(card);
                card.setTranslationX(originalX - newX);
                card.setTranslationY(originalY - newY);
                ViewGroup mainLayout = (ViewGroup)findViewById(R.id.mainLayout);
                mainLayout.getOverlay().add(card);
                card.animate().translationX(0).translationY(0).rotation((float) (Math.random()*180)).withEndAction(() -> {
                    mainLayout.getOverlay().remove(card);
                    playedCardsRow.addView(card);
                });
                return true;
            }
        });

        //TODO remove
        Toast toast = Toast.makeText(getApplicationContext(), card.getTag().toString(),
                Toast.LENGTH_SHORT);
        toast.show();
    }

    public void giveCards(View view) {
        app.gameLogic.dealCards();
    }

    public void hostNewGame(View view) {
        /*
        String id = "JMY9I";
        gameLogic.joinGame(id);
        */

        String id = app.gameLogic.hostGame();

        Toast toast = Toast.makeText(getApplicationContext(), id,
                Toast.LENGTH_LONG);
        toast.show();

    }

    public void setTrump(Card trumpCard) {
        int cardWidth = (int) (screenWidth / 4.5);
        int cardHeight = (int) (screenWidth / 4.5 / cardAspectRatio);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(cardWidth, cardHeight);
        ConstraintLayout trumpCardLayout = findViewById(R.id.trumpCardLayout);
        ImageView cardView = new ImageView(this);
        cardView.setTag(trumpCard.getResourceName());
        cardView.setId(View.generateViewId());
        cardView.setImageResource(trumpCard.getResId());
        cardView.setClickable(false);
        cardView.setLayoutParams(params);
        trumpCardLayout.addView(cardView);
    }


    private float getAbsX( View view ) {
        if(view.getParent() == view.getRootView())
            return view.getX();
        else
            return view.getX() + getAbsX((View)view.getParent());
    }
    private float getAbsY(View view) {
        if(view.getParent() == view.getRootView())
            return view.getY();
        else
            return view.getY() + getAbsY((View)view.getParent());
    }
}