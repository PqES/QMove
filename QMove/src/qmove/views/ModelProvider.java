package qmove.views;

import java.util.ArrayList;


import qmove.core.QMoveHandler;
import qmove.movemethod.MethodsChosen;

public enum ModelProvider {
        INSTANCE;

        private ModelProvider() {
        }

        public ArrayList<MethodsChosen> getMethods() {
                return QMoveHandler.methodsMoved;
        }

}
