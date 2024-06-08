module com.github.tonivade.purecheck {
  exports com.github.tonivade.purecheck;
  exports com.github.tonivade.purecheck.spec;

  requires transitive com.github.tonivade.purefun;
  requires transitive com.github.tonivade.purefun.core;
  requires com.github.tonivade.purefun.effect;
  requires com.github.tonivade.purefun.monad;
  requires transitive com.github.tonivade.purefun.typeclasses;
}