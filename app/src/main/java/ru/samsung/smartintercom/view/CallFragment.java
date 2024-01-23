package ru.samsung.smartintercom.view;

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

        ImageView callImage = view.findViewById(R.id.call_image);
        callImage.setAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.shake_call_animation));
        callImage.setVisibility(View.VISIBLE);

        Button acceptCallButton = view.findViewById(R.id.accept_call_button);
        acceptCallButton.setOnClickListener(v -> {
            _ctx.appState.lastReceivedCallTime.setValue(Instant.now());
            _ctx.navigateToMenuItem.execute(R.id.button_main);
            _ctx.setRemoteIsOpen.execute(true);

            _ctx.onAcceptedCall.execute(null);
        });

        Button declineCallButton = view.findViewById(R.id.decline_call_button);
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