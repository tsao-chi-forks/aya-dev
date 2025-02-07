# Early changelog

This file contains the changelog of the Aya language 0.x.

## 0.13

Upgraded gradle, resolve pattern matching in the resolver instead of the type checker,
moved some code (source locations and generalized tree builder) to `tools`,
generalized the binop parser into `tools`, implemented binop in patterns,
several bug fixes of the distiller, actually use the `opaque` (renamed from `erased`)
and `inline` modifiers in unfolder, improved the parser for literate inline codeblocks,
disable overlapping patterns in function definitions by default (still enabled for conditions),
use first-match semantics on non-overlapping patterns, enabled overlapping patterns with `overlap`
modifier, improved index unification, added support for forced patterns (see [index-unification.md]),
replaced a lot of visitors with pattern matching, inline `as` in patterns during type checking,
added experimental `variable` keyword which is similar to the one in Agda (it lacks several
desirable features such as referring to a generalized variable from a generalized variable),
use `ErrorExpr` instead of `null` for unspecified result type of primitive definitions.

The most notable improvement would be the generalized binary operator parser in `tools`.
It should be _very useful_ to PL implementers using Java 17.

[index-unification.md]: index-unification.md

## 0.12

Actually implemented the inference of type checking ordering,
extracted some tools from `base` to `tools`, allow emission of `Type 0`
in data declarations, suppressed some unnecessary errors, improved
'unresolved meta' error message with a description of their location,
enhanced pretty printing (for `new` expressions, binary operators,
reduced unnecessary parentheses and added necessary parentheses,
eta-contract def-calls), added error message for unknown fields,
make the latex backend automatically insert `\noindent`,
removed jimgui-based tracer (moved to test) for smaller fatJars,
fixed no-arg constructor pattern matching, improved coverage checker
(fixed a divergence case, added fuel for impossible cases elimination),
fixed pattern checker when there are more implicit parameters omitted
in the patterns, added `compareApprox` in `compareUntyped`,
removed `abusing` syntax, refactor `bind` syntax, use the REPL for
pretty-printing options configuration.

Publish the `lsp` module.

## 0.11

Enhanced repl: completion, command system, command parser based on antlr,
tests, cd/pwd/load/etc. commands, redefinitions, highlighting, etc.

Tycking now stores state (term/level equations, solved metas)
in `TyckState` so in the future we may save/restore it.

Some LSP bug fixes, a new 'domination' warning suggested by [@nobodxbodon],
avoid tycking bodies when patterns are malformed, finished all easy
pattern matching to visitor replacements, enhance binop parser with section support,
generate Aya syntax highlight YML during build, Kala-relevant refactoring.

[@nobodxbodon]: https://github.com/nobodxbodon

## 0.10

Performance improvements¸ prototypical repl, binary operator
enhancements, relicense as MIT, substitution bug fixes, minor level
solver improvements, code of conduct & contribution guidelines.

Internal refactoring: `Problem::describe` is now parameterized by
`DistillationOptions`, remove infinity level in universe polymorphism

## 0.9

Some bug fixes, eta rule in pattern unification and `new` components,
renaming in concall and fieldcall, enhance level solver and level
substitution and level syntax, refactor some visitors into pattern
matching, fix comment syntax, enhanced coverage checker with automatic
detection of impossible patterns, concrete expression refactoring,
goal error message enhancements.

## 0.8.3

Initial (de)serialization, some implicits/levels/normalization/unification
relevant bug fixes, removed builtin hlevel, improved error report
(some metas are now inlined), migrated to java 17, replaced some
visitors with pattern matching, tycker is finally bidirectional,
removed tgbot subproject.

## 0.8.2

Some cosmetic improvements to Aya, such as literature mode,
type theoretical enhancements, test refactorings, etc.

## 0.8.1

Pretty printing (including the framework) enhancements,
level solver enhancements, publish to mavenCentral.

## 0.8

This is a quick release after 0.7,
with some improvements on implemented features.

## 0.7

Goal: everything else we need to prepare for being used by other people.

Roadmap: TDD -> library system -> user goals and VSCode LSP ->
packaging Aya as an application that can be installed on a Java-free
machine -> dependency management -> ...

Work done: insertion of implicit at tail, make `RefTerm` typed,
remove termdsl test framework, implement level hierarchy, its inference
and a solver properly, initial `GetTypeVisitor`, make lsp work on
Windows, simplify defeq, multi-location error report, fat jar,
initial binop, unify `new` terms, jlink, remove backslashes in keywords,
delayed error report, chained projection.

## 0.6

Primitive definitions, intervals and Arend coercion, confluence for
conditions, proper absurd patterns, proper holes and pattern unification,
new expressions for records (support a very naive binding technique),
records with conditions, LaTeX backend, internal refactoring.

## 0.5

Coverage and confluence check, prepare for local modules, records
(incomplete), indexed types, styled Doc with HTML backend,
simple unification, qqbot as an optional subproject, upgrade to Java 16 and Gradle 7.0-RC-1,
tons of bug fixes including `LocalVar` comparison bug and term comparison bug.

## 0.4

Definition by pattern matching, conditions, removed `\elim` keyword,
telegram bot, internal refactorings (most notably patterns and decls and defs).

## 0.3

Inductive types resolve/tycking, fncall, improved CLI, tracing for tycking
(both markdown and imgui based visualization), license changed to GPLv3,
desugar refactoring, file-based test framework, tons of bug fixes,
rename mzi -> aya.

## 0.2

Module system, declaration resolution, definition tycking,
simple CLI that just works, simple pattern unification.

## 0.1

Expression tycking, simple DTLC+pi+sigma, expression resolution,
simple conversion check.
