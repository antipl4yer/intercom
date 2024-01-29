**Организация кода**

Данный проект представляет из себя реализацию нативного **Android** приложения **Intercom** (умный домофон).

Для решения данного кейса использовался подход разделения приложения на слои абстракции и использовании **Composition Tree** с единой точкой входа.

В качестве слоя связей выступают реактивные объекты.

Внедрение зависимостей происходит через контекст, в качестве которого выступает класс без методов, в поля которого явно прописываются все зависимости. Дальше контекст внедряется в конструктор (или для слоя **View**метод **setCtx**) принимающего класса.

Код пишется явно, любые зависимости можно явно отследить по коду.

Наша команда использовала язык **Java**.

Используемый подход близок к **MVVM**, но существенно его расширяет и дополняет.

 \
Нужно учитывать, что паттерны проектирования были созданы более 30 лет назад и были заточены под такие языки программирования, как **SmallTalk**, **C**, **C++**.  \
За это время появились языки нового поколения, а **C++** претерпел сильные изменения.  \
Паттерны хоть и устарели, но до сих пор используются в большинстве проектов. 

Мы решили заглянуть в будущее и немного пофантазировать на тему выбора архитектуры.

Всего было выделено **6 слоёв**: 

- **Entity**

- **Pm**

- **View**

- **State**

- **Service**

- Cлой связей (реактивные объекты и **Ctx**). 

Если код приложения представить, как дерево, то **Entity** отвечает за узлы (вершины) этого дерева.  \
Её ответственность создать или получить нужные зависимости и передать ниже по дереву.  \
**Entity** может содержать небольшую бизнес-логику, которая отвечает за создание и инициализацию объектов, которые требуется передать в виде зависимостей ниже.

**Entity** может отвечать за получение **View**, создание других **Entity**, создание **Pm**, создание реактивных объектов, которые будут использоваться для связи в дереве, а также за создание **State**.

Не допускается использования публичных методов/полей.

Все зависимости приходят в контексте и внедряются в конструкторе.

Слой **Pm** (**Presentation Model**) отвечает за чистую бизнес-логику, которая пишется абстрактно, используя реактивные объекты.

Только **Entity** может создавать **Pm**. 

 \
**Pm** может использовать сервисы.  \
**Pm** не может создавать **Entity**, **View**, **Pm**, **State**.  \
Жизненный цикл ограничен **Entity**, которая создала **Pm**.

Не допускается использования публичных методов/полей.

Все зависимости приходят в контексте (**Ctx**) и внедряются в конструкторе.

Слой **View** отвечает за отображение на экране (то, что видит пользователь). **View** не должна содержать бизнес-логику, но может содержать логику работы самого отображения. В текущем проекте **View** - это **Activity**и **Fragment**.

**View** не может создавать **View**, **Entity**, **Pm**, 

Поскольку в нативных **Android** приложениях за этот слой отвечает сама система, то внедрение зависимостей производится через публичный метод **setCtx**, других публичных методов для разработчика **View** содержать не должен.

Условно процесс выглядит так: система переключила **Activity** или **Fragment**, в нашем коде сработал обработчик, дальше нужная **Entity** понимает, с какими **View** она работает, в ней срабатывает подписка, если это её **Activity** или **Fragment**, то можем заполнить контекст и пробросить во **View** зависимости. 

Слой **State** - отвечает чисто за состояние приложения.  \
Никакая логика в нём не допускается.  \
В **State**, в качестве данных используются реактивные свойства (**ReactiveProperty**).  \
Задача **State**инициализировать начальные значения.  \
Все поля **State**должны быть заполнены в конструкторе. 

Поля, которые требуются различным слоям приложения должны быть публичными и помечены **final**, чтобы защититься от замены.

Слой сервисов (**Service**) - отвечает за публичное **API**для стороннего кода, является оберткой над **API**, которые предоставляют внешние библиотеки.

Может иметь публичные поля и методы.

**Service** не может создавать **Entity**, **Pm**, **View**, **State**, **Service**.

Все зависимости приходят в контексте и внедряются в конструкторе.

Слой связей реализован с помощью реактивных объектов из пакета **JavaRx**: **ReactiveCommand** и **ReactiveProperty**. \
Все эти объекты передаются через контексты и внедряются в нужные классы через конструкторы или для слоя **View** через метод **setCtx**.

Оба объекта реализуют концепцию реактивного **Generic** контейнера.

Главное отличие **ReactiveCommand** от **ReactiveProperty** в том, что **ReactiveProperty** хранит последнее значение, любой подписчик сработает при установке значения, даже в том случае, если значение было установлено раньше создания подписчика. 

Подписка сработает 1 раз, если в **ReactiveProperty** 2 раза придет одно и то же значение.

**ReactiveCommand** можно рассматривать, как абстрактный полиморфный single метод.  \
Если подписка произойдет после вызова **execute**, то подписчик не сработает.

Удобно мыслить и писать код в абстрактных категориях, которые на зависят от реализации.

В таком случае получается абстрактный декларативный код, не зависящий от реализации. Также реализуется полиморфизм без применения наследования и использования интерфейсов.

Основное преимущество данного подхода в том, что каждый отдельный слой не зависит от других, но зависит от реактивных объектов, которые абстрактно их связывают.

 \
Также данный подход избавляет разработчика от **Singleton’ов**, **Repository**, **атрибутов, EventBus** и иных подходов, которые могут сильно усложнить разработку, когда над проектом работает несколько человек. 

Примером может являться тот факт, что наличие **Signleton’ов**, **Repository,** **атрибутов, EventBus** обязывают разработчиков:

- использовать только тот фреймворк, где поддерживаются атрибуты, которые прописаны в коде. При переходе на другой фреймворк нужно будет менять атрибуты, если это вообще будет возможно

- использовать много **public** методов/полей, которые могут поставить разработчика в тупик, когда будет непонятно можно ли использовать набор публичных методов объекта класса. 

- неявно использовать связь между подсистемами в проекте, что сильно затрудняет отладку.

В реализованном подходе отпадает необходимость использовать интерфейсы для написания кода приложения, что позволяет сократить кодовую базу в больших проектах. \
Отсоединение/замена любой части приложения будет безболезненной и повлияет лишь на ту роль которую реализует эта часть.

**!При таком подходе реализуется сильная связность и слабое зацепление, что является правилом хорошего тона.**

Для большинства объектов, которые требуют освобождения ресурсов, например подписок, используется реализация интерфейса **Disposable**. 

Для каждого такого объекта необходимо вручную контролировать вызов метода **dispose**, когда объект больше не нужен.

В нашей реализации, для упрощения контроля освобождения ресурсов используется класс **BaseDisposable**, в котором можно запланировать автоматический вызов метода **dispose** для объектов реализующих интерфейс **Disposable** при вызове метода **dispose** класса наследуемого от **BaseDisposable**.

Мы используем наследование **BaseDisposable** для **Entity**, **Pm** слоёв.

Для освобождения ресурсов **View** слоя используются классы: **BaseAppCompatActivityDisposable** и **BaseFragmentDisposable.**

Все **Activity** должны наследоваться от класса **BaseAppCompatActivityDisposable**.

Все Fragment в приложении должны наследоваться от класса **BaseFragmentDisposable**.

В классах **BaseDisposable, BaseAppCompatActivityDisposable, BaseFragmentDisposable** предумотрен специальный **protected** метод **deferDispose**, который добавляет **Disposable** объект в список объектов, для которых будет вызван метод **dispose**.

В данной реализации наша команда ограничилась одной **Activity** (**MainActivity**). Все состояния экрана реализованы через фрагменты.  \
Для реализации состояний экранов, в каждом фрагменте могут содержаться подфрагменты.

В заключении нужно сказать, что по-хорошему папки проекта стоит организовывать исходя из **Composition Tree** проекта, можно использовать одноименные наименования папок: **pm**, **view**, **state**, **entity**, **service**, и т.д.

Но в нашем случае, поскольку предоставленная заготовка для проекта не может меняться, слой **View**находится в папке **ui**.

**Алгоритмизация**

**Работа с слоем связей**

Слой связей представлен набором реактивных объектов: **ReactiveProperty**, **ReactiveCommand**, которые создаются в нужных узлах **Composition Tree**.

Далее в тех классах, которые должны принимать нужные связи с зависимостями прописывается вложенный **static class Ctx**, в котором в явном виде описывается набор зависимостей, с которыми будет работать тот класс, где описываются вложенный. 

Например, **CallEntity** отвечающая за входящие звонки должна принимать на вход:

- **Android** контекст приложения, например для использования **string ресурсов**

- объект базы данных, т.к. это требуется для записи в историю вызовов

- состояние приложения **appState**

- набор реактивных объектов, которые в явном виде приходят из других узлов **CompositionTree: onFragmentViewCreated, navigateToMenuItem, setRemoteIsOpen, onIncomingCall, onMissedCall**.

``` java

public class CallEntity extends BaseDisposable {

	public static class Ctx {
		public Context appContext;
		public AppState appState;
		public IntercomDatabase database;
		public SystemNotificationService systemNotificationService;
		public ReactiveCommand<Fragment> onFragmentViewCreated;
		public ReactiveCommand<Integer> navigateToMenuItem;
		public ReactiveCommand<Boolean> setRemoteIsOpen;
		public ReactiveCommand<Void> onIncomingCall;
		public ReactiveCommand<Void> onMissedCall;
	}

	private final Ctx _ctx;

	public CallEntity(Ctx ctx) {
		…
		Тут можем подписаться на реактивные объекты
	}
	…
}
```
**Работа с View слоем**

Поскольку мы хотим полностью контролировать детерминированное выполнения кода и отслеживать явные связи, то для управления слоем **View** мы придумали механизм, которые позволяет перехватить показ **Activity** и **Fragment** приложения, которое контролирует сама **Android** система.

Под детерминированным выполнением кода понимается, что разработчик может явно через навигатор по коду отследить откуда в **Composition Tree** приходит зависимость.

Перехват осуществляется в классе **AppProxy**, который на вход получается в контексте реактивные объекты: \
``` java
public ReactiveCommand<Activity> onActivityStarted;
public ReactiveCommand<Fragment> onFragmentViewCreated;
```

Поскольку за получения информации о **View**слое отвечает **Entity**, то в нужных **Entity** мы подписываемся на то, с чем работает **Entity**, например **MainEntity** работает с **MainActivity** и её **Fragment’ами** и именно **MainEntity** ответственна за внедрение зависимостей в **MainActivity** и соответствующие **Fragment’ы**.

Пример конструктора в **MainEntity**:
``` java
deferDispose(ctx.onActivityStarted.subscribe(activity -> {
	if (activity instanceof MainActivity) {
		MainActivity.Ctx mainActivityCtx = new MainActivity.Ctx();
		mainActivityCtx.navigateToMenuItem = _ctx.navigateToMenuItem;
		mainActivityCtx.systemNotificationService = _ctx.systemNotificationService;

		((MainActivity) activity).setCtx(mainActivityCtx);
		return;
	}
}));

deferDispose(ctx.onFragmentViewCreated.subscribe(fragment -> {
	if (fragment instanceof MainFragment) {
		MainFragment.Ctx mainFragmentCtx = new MainFragment.Ctx();
		mainFragmentCtx.appState = _ctx.appState;
		mainFragmentCtx.flushAppState = _ctx.flushAppState;
		mainFragmentCtx.remoteActionStatus = _ctx.remoteActionStatus;
		mainFragmentCtx.loadStatus = _ctx.loadStatus;
		mainFragmentCtx.lastErrorDescription = _ctx.lastErrorDescription;
		mainFragmentCtx.isCurrentSettingsValid = _ctx.isCurrentSettingsValid;

		((MainFragment) fragment).setCtx(mainFragmentCtx);
		return;
	}

	if (fragment instanceof FirstRunFragment) {
		FirstRunFragment.Ctx firstRunFragmentCtx = new FirstRunFragment.Ctx();
		firstRunFragmentCtx.navigateToMenuItem = _ctx.navigateToMenuItem;

		((FirstRunFragment) fragment).setCtx(firstRunFragmentCtx);
		return;
	}

	if (fragment instanceof InfoFragment) {
		InfoFragment.Ctx infoFragmentCtx = new InfoFragment.Ctx();
		infoFragmentCtx.appState = _ctx.appState;
		infoFragmentCtx.loadInfo = _ctx.loadInfo;
		infoFragmentCtx.takePhotoStatus = _ctx.takePhotoStatus;

		((InfoFragment) fragment).setCtx(infoFragmentCtx);
		return;
	}

	if (fragment instanceof InfoImageFragment) {
		InfoImageFragment.Ctx infoImageFragmentCtx = new InfoImageFragment.Ctx();
		infoImageFragmentCtx.appState = _ctx.appState;
		infoImageFragmentCtx.takeRemotePhoto = _ctx.takeRemotePhoto;
		infoImageFragmentCtx.takePhotoStatus = _ctx.takePhotoStatus;
		infoImageFragmentCtx.lastErrorDescription = _ctx.lastErrorDescription;

		((InfoImageFragment) fragment).setCtx(infoImageFragmentCtx);
		return;
	}

	if (fragment instanceof ErrorFragment) {
		ErrorFragment.Ctx errorFragmentCtx = new ErrorFragment.Ctx();
		errorFragmentCtx.appState = _ctx.appState;
		errorFragmentCtx.loadInfo = _ctx.loadInfo;
		errorFragmentCtx.lastErrorDescription = _ctx.lastErrorDescription;

		((ErrorFragment) fragment).setCtx(errorFragmentCtx);
		return;
	}
}));
```

**Работа с слоем State**

Для работы с слоем **State** выделен отдельный класс **AppState**, он должен пробрасываться в нужные узлы **Composition Tree** в явном виде через соответствующие контексты. 

Слой **State** в нашем случае персистентен и периодически сохраняется на персистентное файловое хранилище мобильного устройства. 

За логику выполнения сохранения отвечает класс **AppStatePersistentPm**, одной из зависимостей которого является **ReactiveCommand flushAppState**. Команда **flushAppState** немедленно сохраняет (сереализует в **Json**) текущее состояние приложения в персистентное файловое хранилище мобильного устройства.

При старте приложения выполняется логика загрузки (десереализации из **Json**) уже существующего состояния, либо создание нового состояния (если это первый старт).

Далее, те узлы **Composition Tree**, которым нужно работать с состоянием получают его в явном виде в контексте.

В заключении нужно сказать, что нужно стараться писать самодокументируемый код, который избавит разработчиков от лишней нагрузки отслеживания и документировании изменений в коде.

**Работа с данными**

В данной реализации приложения используется два хранилища:

- Реляционная бд **Room** для хранения истории вызовов

- **JSON** на персистентном хранилище устройства, использующий пакет **Gson** и кастомные конвертеры для нестандартных типов данных: **Bitmap**, **Instant**, реактивных объектов, и т.д.

Работа с базой данных **Room** осуществляется при взаимодействии с входящими вызовами: принятие, отклонение, пропуск.

Сохранение состояния приложения производится через пакет **Gson** и хранится в персистентном хранилище на телефоне в виде **JSON**.

Для сериализации **Generic ReactiveProperty** были написаны следующие конвертеры:

- **InstantConverter**, конвертер для типа **Instant**

- **BitmapConverter**, конвертер для типа **Btimap**

- Обобщенный **ReactivePropertyConverter** для не примитивных типов Generic контейнера: **String**, **Integer**, **Instant**, **Bitmap**, и т.д.

Инициализация **Json** системы сериализации/десериализации производится через статический метод **setup** класса **Json** в **RootEntity**, сразу после запуска приложения. 

Сначала регистрируются конвертеры для не реактивных не примитивных типов, далее через обобщенный класс **ReactivePropertyConverter** регистрируются конвертеры для реактивных типов: \
 \
``` java
private static void registerConverters() {
	InstantConverter.registerConverter(_gsonBuilder);
	BitmapConverter.registerConverter(_gsonBuilder);

	ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<String>>() {
	});
	ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<Number>>() {
	});
	ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<Boolean>>() {
	});
	ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<Integer>>() {
	});
	ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<LoadStatus>>() {
	});
	ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<Instant>>() {
	});
	ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<Bitmap>>() {
	});
}
```
**Визуализация**

Для перехода между экранами пользователь всегда может использовать нижнюю панель навигации в которой всегда доступны 3 кнопки: “**Главная**”, “**История**”, “**Настройки**”.

При запуске приложения пользователь увидит экран приветствия.

Если версия **Android >= Tiramisu**, то покажется нативное окно с разрешением отправки локальных нотификаций.

![Enter image alt description](Images/szI_Image_1.jpeg)

![Enter image alt description](Images/iLP_Image_2.jpeg)

Далее необходимо нажать кнопку “**Перейти к настройкам**”.

![Enter image alt description](Images/ZPL_Image_3.jpeg)

Приложение автоматически валидирует введенные данные о доме и квартире и там, где это возможно ограничивает ввод. Например в поле ввода дома можно ввести значение: “32/”, т.к. можно продолжить ввод, например дописав “a”, тогда значение будет валидно “32/a”.

Для каждого поля существует подсказка, которая появляется при неверном вводе. Если введенные данные некорректны, то кнопка “**Сохранить**” будет недоступна.

![Enter image alt description](Images/4mF_Image_4.jpeg)

![Enter image alt description](Images/ZN4_Image_5.jpeg)

После сохранения настроек, введенные данные будут сохранены в состоянии приложения и после перезапуска приложения эти настройки будут восстановлены. Далее пользователь должен вернуться на главный экран нажав системную кнопку “**Back**”, либо через навигационную панель, кнопка “**Главная**”. Если данные введены корректно, то будет выполнено подключение к серверу и получена информация о домофоне.

![Enter image alt description](Images/cty_Image_6.jpeg)

Далее пользователь может получить изображение с домофона, нажав на кнопку “**Получить фото**”. При повторном нажатии кнопки будет получено новое изображение.

![Enter image alt description](Images/xXR_Image_7.jpeg)

Каждая операция: получения информации о домофоне или загрузки изображения сопровождается состоянием загрузки, при ожидании пользователь увидит эти состояния, в зависимости от того, какую операцию совершил пользователь.

![Enter image alt description](Images/R1b_Image_8.jpeg)

![Enter image alt description](Images/nYR_Image_9.jpeg)


В техническом задании описана ситуация, когда на главном экране должна вывестись ошибка о некорректных настройках домофона. Она может возникнуть, когда пользователь сохранил корректные настройки, потом вернулся в настройки и например ввел: “32/”, и вернулся обратно на главный экран, в таком случае приложение выдаст ошибку.

![Enter image alt description](Images/zgY_Image_10.jpeg)

Также при возникновении других ошибок: отсутствие сети **Интернет**, ошибка подключения и т.д., будет также выведена информация на главный экран.

![Enter image alt description](Images/sDV_Image_11.jpeg)

![Enter image alt description](Images/JhN_Image_12.jpeg)

Если при получении изображения с домофона возникает ошибка, то ошибка выводится в блок с изображением.

![Enter image alt description](Images/c2d_Image_13.jpeg)

Если поступает входящий звонок, то показывается экран с входящим звонком.

![Enter image alt description](Images/uJh_Image_14.jpeg)

Далее есть несколько вариантов.

Первый вариант входящий звонок принят, открыли дверь.

В этом случае пользователь нажимает на кнопку “**Открыть**”.

В этом случае, при успешном исходящем запросе на открытии двери, в истории пользователь увидит, что дверь была открыта.

История сохраняется на персистентное хранилища мобильного устройства и будет доступна при перезаходе в приложение.

![Enter image alt description](Images/kBL_Image_15.jpeg)

Входящий звонок сброшен.

В этом случае пользователь, при переходе в историю увидит, что звонок был сброшен.

![Enter image alt description](Images/yEA_Image_16.jpeg)

Входящий звонок был проигнорирован, пользователь не совершил никаких действий в течение 15 сек.

В таком случае при будет выведено локальное пуш уведомление с пропущенным вызовом и информация о пропущенном вызове попадет в историю со статусом “пропущен”.

![Enter image alt description](Images/cDg_Image_17.jpeg)

![Enter image alt description](Images/0E4_Image_18.jpeg)

Если пользователь при входящим вызове перешел на другой экран, например в историю, то по истечении 15 сек в истории появится пропущенный вызов.

![Enter image alt description](Images/JJr_Image_19.jpeg)
