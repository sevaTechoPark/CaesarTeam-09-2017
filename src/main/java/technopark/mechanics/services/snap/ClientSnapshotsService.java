package technopark.mechanics.services.snap;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import technopark.mechanics.models.Coords;
import technopark.mechanics.models.Move;
import technopark.mechanics.models.part.MovePart;
import technopark.mechanics.models.player.GameUser;
import technopark.mechanics.models.part.MechanicPart;
import technopark.mechanics.models.part.MousePart;
import technopark.mechanics.models.session.GameSession;
import technopark.mechanics.requests.ClientSnap;
import technopark.account.dao.AccountDao;
import technopark.mechanics.models.id.Id;

import java.util.*;

// Not thread safe! Per models mechanic service

@Service
public class ClientSnapshotsService {

    private final Map<Id<AccountDao>, List<ClientSnap>> snaps = new HashMap<>();

    public void pushClientSnap(@NotNull Id<AccountDao> user, @NotNull ClientSnap snap) {
        this.snaps.putIfAbsent(user, new ArrayList<>());
        final List<ClientSnap> clientSnaps = snaps.get(user);
        clientSnaps.add(snap);
    }

    @NotNull
    public List<ClientSnap> getSnapForUser(@NotNull Id<AccountDao> user) {
        return snaps.getOrDefault(user, Collections.emptyList());
    }

    public void processSnapshotsFor(@NotNull GameSession gameSession) {
        final Collection<GameUser> players = new ArrayList<>();
        players.add(gameSession.getFirst());
        if (!gameSession.isSinglePlay()) {
            players.add(gameSession.getSecond());
        }

        for (GameUser player : players) {
            final List<ClientSnap> playerSnaps = getSnapForUser(player.getAccountId());
            processFone(gameSession, player);
            if (playerSnaps.isEmpty()) {
                continue;
            }

             playerSnaps.stream().filter(ClientSnap::isDrill).findFirst().ifPresent(snap -> processClick(snap, gameSession, player));
             playerSnaps.stream().filter(ClientSnap::isMove).findFirst().ifPresent(snap -> processMove(snap, gameSession, player));
             playerSnaps.stream().filter(ClientSnap::isJump).findFirst().ifPresent(snap -> processJump(gameSession, player));
            final ClientSnap lastSnap = playerSnaps.get(playerSnaps.size() - 1);
            processMouseMove(player, lastSnap.getMouse());
            processPlayerMove(player, lastSnap.getMoveTo());
        }
    }

    private void processFone(@NotNull GameSession gameSession, GameUser gameUser) {
        Id<AccountDao> user = gameUser.getAccountId();
        gameSession.getMapForGame().checkGravity(user);
        gameSession.getMapForGame().checkJump(user);
        gameSession.getMapForGame().checkBonus(user);
    }

    private void processJump(@NotNull GameSession gameSession, @NotNull GameUser gameUser) {
        final MechanicPart mechanicPart = gameUser.claimPart(MechanicPart.class);
        if (mechanicPart.tryJump()) {
            gameSession.getMapForGame().startJump(gameUser.getAccountId());
        }
    }

    // сделать действия по клику
    private void processClick(@NotNull ClientSnap snap, @NotNull GameSession gameSession, @NotNull GameUser gameUser) {
        final MechanicPart mechanicPart = gameUser.claimPart(MechanicPart.class);
        if (mechanicPart.tryDrill()) {
            gameSession.getMapForGame().drillAt(snap.getMouse(), gameUser.getAccountId());
        }
    }
    // сохранить текущий клик
    private void processMouseMove(@NotNull GameUser gameUser, @NotNull Coords mouse) {
        gameUser.claimPart(MousePart.class).setMouse(mouse);
    }

    // сделать действия по нажатию на клаву
    private void processMove(@NotNull ClientSnap snap, @NotNull GameSession gameSession, @NotNull GameUser gameUser) {
        final MechanicPart mechanicPart = gameUser.claimPart(MechanicPart.class);
        if (mechanicPart.tryMove()) {
            gameSession.getMapForGame().moveTo(snap.getMoveTo(), gameUser.getAccountId());
        }
    }
    // сохранить текущее состояние клавы
    private void processPlayerMove(@NotNull GameUser gameUser, @NotNull Move move) {
        gameUser.claimPart(MovePart.class).setMove(move);
    }

    public void clearForUser(Id<AccountDao> userProfileId) {
        snaps.remove(userProfileId);
    }

    public void reset() {
        snaps.clear();
    }
}
