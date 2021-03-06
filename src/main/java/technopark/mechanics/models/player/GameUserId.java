package technopark.mechanics.models.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import technopark.mechanics.models.Snap;
import technopark.account.dao.AccountDao;
import technopark.mechanics.models.id.Id;


public class GameUserId extends GameObject {
    @Nullable
    private Id<AccountDao> gameUserId;

    @Nullable
    public Id<AccountDao> getGameUserId() {
        return gameUserId;
    }

    public void setGameUserId(@Nullable Id<AccountDao> gameUserId) {
        this.gameUserId = gameUserId;
    }

    @Override
    @NotNull
    public GameUserIdSnap getSnap() {
        return new GameUserIdSnap(this);
    }

    public static final class GameUserIdSnap implements Snap<GameUserId> {

        @Nullable
        private final Id<AccountDao> userId;

        public GameUserIdSnap(@NotNull GameUserId gameUserId) {
            this.userId = gameUserId.gameUserId;
        }

        @Nullable
        public Id<AccountDao> getOccupant() {
            return userId;
        }
    }

    @Override
    public String toString() {
        return "GameUserId{"
                + "gameUserId=" + gameUserId
                + '}';
    }
}
