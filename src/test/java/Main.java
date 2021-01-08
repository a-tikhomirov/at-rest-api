import ru.at.rest.api.cucumber.CoreEnvironment;
import ru.at.rest.api.cucumber.CoreScenario;

import static ru.at.rest.api.utils.PropertyLoader.loadProperty;
import static ru.at.rest.api.cucumber.ScopedVariables.resolveVars;

public class Main {
    public static CoreScenario coreScenario = CoreScenario.getInstance();
    public static void main(String[] args) {
        coreScenario.setEnvironment(new CoreEnvironment(null));
        String test = resolveVars("{imgur.api.host}:{test}:{imgur.api.host}:{test}");
        System.out.println(test);
//        coreScenario.getEnvironment().setVar("imageHash", "test_hash");
//        test = resolveVars(loadProperty("imgur.api.image.update.authed"));
//        System.out.println(test);
    }
}
