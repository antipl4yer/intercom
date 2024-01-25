package ru.samsung.smartintercom.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.framework.BaseFragmentDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.state.AppState;

import java.time.Instant;

public class CallFragment extends BaseFragmentDisposable {
    public static class Ctx {
        public AppState appState;
        public ReactiveCommand<Integer> navigateToMenuItem;
        public ReactiveCommand<Boolean> setRemoteIsOpen;
        public ReactiveCommand<Void> onIncomingCall;
        public ReactiveCommand<Void> onMissedCall;
        public ReactiveCommand<Void> onAcceptedCall;
        public ReactiveCommand<Void> onDeclinedCall;
    }

    private Ctx _ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_call, container, false);
    }

    public void setCtx(Ctx ctx){
        _ctx = ctx;

        View view = getView();

        LinearLayout callActionContainer = view.findViewById(R.id.call_action_container);
        callActionContainer.setVisibility(View.VISIBLE);

        TextView missedCallTextView = view.findViewById(R.id.missed_call);
        missedCallTextView.setVisibility(View.GONE);

        Context context = this.getContext();

        ImageView callImage = view.findViewById(R.id.call_image);
        callImage.setVisibility(View.VISIBLE);
        callImage.setAnimation(AnimationUtils.loadAnimation(context, R.anim.shake_call_animation));

        deferDispose(_ctx.onIncomingCall.subscribe(unused -> {
            callActionContainer.setVisibility(View.VISIBLE);
            missedCallTextView.setVisibility(View.GONE);
            callImage.setVisibility(View.VISIBLE);
            callImage.setAnimation(AnimationUtils.loadAnimation(context, R.anim.shake_call_animation));
        }));

        Button acceptCallButton = view.findViewById(R.id.button_open);
        acceptCallButton.setOnClickListener(v -> {
            _ctx.appState.lastReceivedCallTime.setValue(Instant.now());
            _ctx.navigateToMenuItem.execute(R.id.button_main);
            _ctx.setRemoteIsOpen.execute(true);

            _ctx.onAcceptedCall.execute(null);
        });

        Button declineCallButton = view.findViewById(R.id.button_close);
        declineCallButton.setOnClickListener(v -> {
            _ctx.appState.lastReceivedCallTime.setValue(Instant.now());
            _ctx.navigateToMenuItem.execute(R.id.button_main);
            _ctx.setRemoteIsOpen.execute(true);

            _ctx.onDeclinedCall.execute(null);
        });

        deferDispose(_ctx.onMissedCall.subscribe(unused -> {
            callActionContainer.setVisibility(View.GONE);
            missedCallTextView.setVisibility(View.VISIBLE);
            callImage.setAnimation(null);
            callImage.setVisibility(View.GONE);
        }));
    }
}