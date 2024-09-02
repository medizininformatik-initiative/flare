package de.medizininformatikinitiative.flare.model.mapping;

import de.medizininformatikinitiative.flare.model.sq.ContextualTermCode;

import java.util.List;
import java.util.stream.Stream;

public record MappingTreeBase(List<MappingTreeModuleRoot> moduleRoots) {

    public Stream<ContextualTermCode> expand(ContextualTermCode termCode) {
        var key = termCode.termCode().code();

        return moduleRoots.stream().flatMap(moduleRoot ->
                isModuleMatching(termCode, moduleRoot) ? moduleRoot.expand(key) : Stream.empty());
    }

    private boolean isModuleMatching(ContextualTermCode termCode, MappingTreeModuleRoot moduleRoot) {
        return termCode.context().equals(moduleRoot.context()) &&
                moduleRoot.system().equals(termCode.termCode().system()) &&
                moduleRoot.entries().containsKey(termCode.termCode().code());
    }
}
